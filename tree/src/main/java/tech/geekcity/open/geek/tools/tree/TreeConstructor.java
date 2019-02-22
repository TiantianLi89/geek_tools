package tech.geekcity.open.geek.tools.tree;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

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
         * @return true if <code>parent</code> node is the parent of <code>child</code> node; otherwise false
         */
        boolean isParent(Node<DataType> parent, Node<DataType> child);

        /**
         * <p>
         * omit the node added before according to this method
         * </p>
         * <p>
         * this method should satisfy that
         * <code>anotherNode</code> should be equal to <code>node</code>
         * if and only if <code>node</code>equals to <code>anotherNode</code>
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
         * @return the <code>RelationShip</code> between two input nodes
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

    private final NodeComparator<DataType> nodeComparator;
    private final List<List<Node<DataType>>> nodeListPool = new ArrayList<>();

    public TreeConstructor(NodeComparator<DataType> nodeComparator) {
        this.nodeComparator = nodeComparator;
    }

    public void clear() {
        nodeListPool.clear();
    }

    public void add(Node<DataType> nodeAdded) {
        if (nodeListPool.stream()
                .flatMap(Collection::stream)
                .anyMatch(node -> nodeComparator.equals(node, nodeAdded))) {
            return;
        }
        List<List<Node<DataType>>> matchedNodeLists = nodeListPool.stream()
                .filter(nodeList
                        -> nodeList.stream()
                        .map(node -> nodeComparator.relationShip(nodeAdded, node))
                        .anyMatch(relationShip -> EnumSet
                                .of(NodeComparator.RelationShip.PARENT, NodeComparator.RelationShip.CHILD)
                                .contains(relationShip)))
                .collect(Collectors.toList());
        if (matchedNodeLists.isEmpty()) {
            // add nodeAdded, as a list, to nodeListPool if not any nodeList matches
            nodeListPool.add(new ArrayList<>(Collections.singletonList(nodeAdded)));
        } else {
            // otherwise, merge the matchedNodeLists to one list and add the node to the merged list
            List<Node<DataType>> firstNodeListMatched = matchedNodeLists.get(0);
            firstNodeListMatched.add(nodeAdded);
            matchedNodeLists.stream()
                    .skip(1)
                    .forEach(nodeList -> {
                        firstNodeListMatched.addAll(nodeList);
                        nodeListPool.remove(nodeList);
                    });
        }
    }

    public List<Node<DataType>> treeList() {
        return nodeListPool
                .stream()
                .map(nodeList -> {
                    // won't happen according to the merge rule in add method
                    if (nodeList.isEmpty()) {
                        return null;
                    }
                    List<Node<DataType>> linkedNodeList = new ArrayList<>(
                            Collections.singletonList(
                                    nodeList.get(0)));
                    nodeList.stream()
                            // skip first one which already added to linkedNodeList
                            .skip(1)
                            // link each node to linkedNodeList
                            .forEach(node -> {
                                linkedNodeList.forEach(linkedNode -> {
                                    if (nodeComparator.isParent(node, linkedNode)) {
                                        node.addChild(linkedNode);
                                    }
                                    if (nodeComparator.isParent(linkedNode, node)) {
                                        linkedNode.addChild(node);
                                    }
                                });
                                linkedNodeList.add(node);
                            });
                    List<Node<DataType>> rootNodeList = linkedNodeList.stream()
                            .filter(Node::root)
                            .collect(Collectors.toList());
                    if (rootNodeList.isEmpty()) {
                        throw new RuntimeException(
                                String.format("no rootNode found in the nodeList(%s)", nodeList));
                    }
                    // won't happen according to the merge rule in add method
                    if (rootNodeList.size() > 1) {
                        throw new RuntimeException(
                                String.format("more than one root node found in nodeList(%s)", nodeList));
                    }
                    return rootNodeList.get(0);
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}