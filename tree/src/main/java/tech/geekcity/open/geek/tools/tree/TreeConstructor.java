package tech.geekcity.open.geek.tools.tree;

import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ben.wangz
 */
public class TreeConstructor<DataType extends Serializable> implements Serializable {
    public interface NodeComparator<DataType extends Serializable> {
        enum RelationShip {
            PARENT,
            CHILD,
            NONE
        }

        /**
         * this method should satisfy that one node has only one parent
         *
         * @param parent the parent node to test
         * @param child  the child node to test
         * @return true if {@code parent} node is the parent of {@code child} node; otherwise false
         */
        boolean isParent(Node<DataType> parent, Node<DataType> child);

        /**
         * <p>
         * omit the node added before according to this method
         * </p>
         * <p>
         * this method should satisfy that
         * {@code anotherNode} should be equal to {@code node}
         * if and only if {@code node} equals to {@code anotherNode}
         * </p>
         *
         * @param node        one node
         * @param anotherNode another node to compare
         * @return true if two nodes are equal to each anotherNode
         */
        boolean equals(Node<DataType> node, Node<DataType> anotherNode);

        /**
         * NOTE: do not override this method if not necessary
         *
         * @param node  any node
         * @param other anther node
         * @return the {@code RelationShip} between two input nodes
         */
        default RelationShip relationShip(
                Node<DataType> node,
                Node<DataType> other) {
            if (isParent(node, other) && isParent(other, node)) {
                throw new RuntimeException(
                        String.format(
                                "not support that one node(%s) is other node(%s)'s both father and child",
                                node, other));
            }
            if (isParent(node, other)) {
                return RelationShip.PARENT;
            }
            if (isParent(other, node)) {
                return RelationShip.CHILD;
            }
            return RelationShip.NONE;
        }
    }

    private static final EnumSet<NodeComparator.RelationShip> LINKED_RELATION_SHIP
            = EnumSet.of(NodeComparator.RelationShip.PARENT, NodeComparator.RelationShip.CHILD);
    private final NodeComparator<DataType> nodeComparator;
    // format id -> {location, node}
    private final Map<String, Pair<Long, Node<DataType>>> linkableLocation4IdKeyed = new HashMap<>();
    // format parent -> List<{location, node}>
    private final Map<String, List<Pair<Long, Node<DataType>>>> linkableLocation4ParentKeyed = new HashMap<>();
    // format List<{location, node}
    private List<Pair<Long, Node<DataType>>> notLinkableDataNodeLocation = new ArrayList<>();
    private final AtomicLong locationCounter = new AtomicLong(0L);

    public TreeConstructor(NodeComparator<DataType> nodeComparator) {
        this.nodeComparator = nodeComparator;
    }

    public void clear() {
        linkableLocation4IdKeyed.clear();
        linkableLocation4ParentKeyed.clear();
        notLinkableDataNodeLocation.clear();
    }

    public void add(Node<DataType> nodeToAdd) {
        add(nodeToAdd, true);
    }

    /**
     * <p>
     * if linkableDataTypeOptimize and {@code nodeToAdd} instanceof {@link LinkableDataType},
     * the constructor will use the optimized method to add node
     * </p>
     *
     * @param nodeToAdd                node to add
     * @param linkableDataTypeOptimize turn on optimization or not
     * @see LinkableDataType
     */
    public void add(Node<DataType> nodeToAdd, boolean linkableDataTypeOptimize) {
        // omit any equal node
        if (Stream
                .concat(
                        linkableLocation4IdKeyed
                                .entrySet()
                                .stream()
                                .map(entry -> entry.getValue().getRight()),
                        notLinkableDataNodeLocation
                                .stream()
                                .map(Pair::getRight))
                .anyMatch(node -> nodeComparator.equals(node, nodeToAdd))) {
            return;
        }
        boolean canOptimize = linkableDataTypeOptimize && (nodeToAdd.data() instanceof LinkableDataType);
        List<Long> matchedLocationList = canOptimize ? optimizedSearchAndLink(nodeToAdd) : searchAndLink(nodeToAdd);
        modifyContainers(canOptimize, nodeToAdd, matchedLocationList);
    }

    public List<Node<DataType>> treeList() {
        Map<Long, List<Node<DataType>>> groupList = Stream.concat(
                linkableLocation4IdKeyed.entrySet().stream().map(Map.Entry::getValue),
                notLinkableDataNodeLocation.stream())
                .collect(Collectors.toMap(
                        Pair::getLeft,
                        nodePair -> new ArrayList<>(Collections.singletonList(nodePair.getRight())),
                        (oldValue, newValue) -> {
                            oldValue.addAll(newValue);
                            return oldValue;
                        }));
        return groupList.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .map(group -> {
                    List<Node<DataType>> rootNodeList = group.stream()
                            .filter(Node::root)
                            .collect(Collectors.toList());
                    if (rootNodeList.isEmpty()) {
                        throw new RuntimeException(
                                String.format("no rootNode found in the group(%s)", group));
                    }
                    // won't happen according to the merge rule in add method
                    if (rootNodeList.size() > 1) {
                        throw new RuntimeException(
                                String.format("more than one root node found in group(%s)", group));
                    }
                    return rootNodeList.get(0);
                }).collect(Collectors.toList());
    }

    private List<Long> searchAndLink(Node<DataType> nodeToAdd) {
        List<Pair<Long, Node<DataType>>> matchedNodePairList = Stream.concat(Stream.concat(
                linkableLocation4IdKeyed.entrySet().stream().map(Map.Entry::getValue),
                linkableLocation4ParentKeyed.entrySet().stream().flatMap(entry -> entry.getValue().stream())),
                notLinkableDataNodeLocation.stream())
                .filter(nodePair -> LINKED_RELATION_SHIP.contains(
                        nodeComparator.relationShip(nodeToAdd, nodePair.getRight())))
                .collect(Collectors.toList());
        matchedNodePairList.stream()
                .map(Pair::getRight)
                .forEach(relationalNode -> {
                    switch (nodeComparator.relationShip(nodeToAdd, relationalNode)) {
                        case PARENT:
                            nodeToAdd.addChild(relationalNode);
                            break;
                        case CHILD:
                            relationalNode.addChild(nodeToAdd);
                            break;
                    }
                });
        return matchedNodePairList.stream()
                .map(Pair::getLeft)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Long> optimizedSearchAndLink(Node<DataType> nodeToAdd) {
        Pair<Long, Node<DataType>> parentNodePair
                = linkableLocation4IdKeyed.get(((LinkableDataType) nodeToAdd.data()).parentId());
        List<Pair<Long, Node<DataType>>> childNodePairList
                = linkableLocation4ParentKeyed.get(((LinkableDataType) nodeToAdd.data()).id());
        List<Pair<Long, Node<DataType>>> relationalNodePairList = notLinkableDataNodeLocation
                .stream()
                .filter(nodePair
                        -> LINKED_RELATION_SHIP.contains(
                        nodeComparator.relationShip(nodeToAdd, nodePair.getRight())))
                .collect(Collectors.toList());
        if (null != parentNodePair) {
            parentNodePair.getRight().addChild(nodeToAdd);
        }
        if (null != childNodePairList) {
            childNodePairList.forEach(childNodePair -> nodeToAdd.addChild(childNodePair.getRight()));
        }
        relationalNodePairList.stream()
                .map(Pair::getRight)
                .forEach(relationalNode -> {
                    switch (nodeComparator.relationShip(nodeToAdd, relationalNode)) {
                        case PARENT:
                            nodeToAdd.addChild(relationalNode);
                            break;
                        case CHILD:
                            relationalNode.addChild(nodeToAdd);
                            break;
                    }
                });
        return Stream
                .concat(
                        Stream.concat(
                                null == parentNodePair
                                        ? Stream.empty() : Stream.of(parentNodePair.getLeft()),
                                null == childNodePairList
                                        ? Stream.empty() : childNodePairList.stream().map(Pair::getLeft)),
                        relationalNodePairList.stream().map(Pair::getLeft))
                .distinct()
                .collect(Collectors.toList());
    }

    private void modifyContainers(boolean canOptimize, Node<DataType> nodeToAdd, List<Long> matchedLocationList) {
        if (matchedLocationList.isEmpty()) {
            // add with a new location
            add(canOptimize, nodeToAdd, locationCounter.getAndIncrement());
            return;
        }
        if (1 == matchedLocationList.size()) {
            // add to an existing location
            add(canOptimize, nodeToAdd, matchedLocationList.get(0));
            return;
        }
        // add to an existing location(first) and merge other locations to it
        long firstMatchLocation = matchedLocationList.get(0);
        linkableLocation4IdKeyed.entrySet()
                .forEach(nodePairEntry -> {
                    Pair<Long, Node<DataType>> nodePair = nodePairEntry.getValue();
                    if (matchedLocationList.contains(nodePair.getLeft())) {
                        nodePairEntry.setValue(Pair.of(firstMatchLocation, nodePair.getRight()));
                    }
                });
        linkableLocation4ParentKeyed.entrySet()
                .forEach(entry
                        -> entry.setValue(
                        entry.getValue()
                                .stream()
                                .map(childNodePair
                                        -> matchedLocationList.contains(childNodePair.getLeft())
                                        ? Pair.of(firstMatchLocation, childNodePair.getRight())
                                        : childNodePair)
                                .collect(Collectors.toList())));
        notLinkableDataNodeLocation = notLinkableDataNodeLocation.stream()
                .map(nodePair
                        -> matchedLocationList.contains(nodePair.getLeft())
                        ? Pair.of(firstMatchLocation, nodePair.getRight())
                        : nodePair)
                .collect(Collectors.toList());
        add(canOptimize, nodeToAdd, firstMatchLocation);
    }

    private void add(boolean canOptimize, Node<DataType> nodeToAdd, long locationToAdd) {
        if (canOptimize) {
            linkableLocation4IdKeyed.put(
                    ((LinkableDataType) nodeToAdd.data()).id(),
                    Pair.of(locationToAdd, nodeToAdd));
            linkableLocation4ParentKeyed.merge(
                    ((LinkableDataType) nodeToAdd.data()).parentId(),
                    new ArrayList<>(Collections.singletonList(Pair.of(locationToAdd, nodeToAdd))),
                    (oldValue, newValue) -> {
                        oldValue.addAll(newValue);
                        return oldValue;
                    });
        } else {
            notLinkableDataNodeLocation.add(Pair.of(locationToAdd, nodeToAdd));
        }
    }
}