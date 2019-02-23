package tech.geekcity.open.geek.tools.tree;

import com.google.common.collect.TreeTraverser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author ben.wangz
 */
public class TreeConstructorTest {
    private static class LinkableData implements LinkableDataType {
        private String id;
        private String parentId;

        public LinkableData(String id, String parentId) {
            this.id = id;
            this.parentId = parentId;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String parentId() {
            return parentId;
        }
    }

    private Node<String> stringTreeRoot;

    @Before
    public void before() throws Exception {
        stringTreeRoot = new Node<>("h")
                .addChild(
                        new Node<>("d")
                                .addChild(new Node<>("a"))
                                .addChild(new Node<>("b"))
                                .addChild(new Node<>("c")))
                .addChild(
                        new Node<>("e"))
                .addChild(
                        new Node("g")
                                .addChild(new Node("f")));
    }

    @Test
    public void test() {
        TreeConstructor<Node<String>> treeConstructor = new TreeConstructor<>(
                new TreeConstructor.NodeComparator<Node<String>>() {
                    @Override
                    public boolean isParent(Node<Node<String>> parent, Node<Node<String>> child) {
                        return Objects.equals(parent.data(), child.data().parent());
                    }

                    @Override
                    public boolean equals(Node<Node<String>> node, Node<Node<String>> anotherNode) {
                        return Objects.equals(
                                node.data().data(),
                                anotherNode.data().data());
                    }
                });
        TreeConstructor<LinkableData> linkableDataTreeConstructor
                = new TreeConstructor<>(LinkableDataType.nodeComparator());

        TreeTraverser.<Node<String>>using(Node::children)
                .postOrderTraversal(stringTreeRoot)
                .forEach(node -> {
                    treeConstructor.add(new Node<>(node));
                    linkableDataTreeConstructor.add(
                            new Node<>(new LinkableData(node.data(), node.parent() == null ? "" : node.parent().data())));
                });
        List<Node<Node<String>>> treeList = treeConstructor.treeList();
        List<Node<LinkableData>> linkableTreeList = linkableDataTreeConstructor.treeList();
        Assert.assertEquals(1, treeList.size());
        Assert.assertEquals(1, linkableTreeList.size());
        Assert.assertEquals(
                StreamSupport.stream(
                        TreeTraverser.<Node<Node<String>>>using(Node::children)
                                .breadthFirstTraversal(treeList.get(0)).spliterator(),
                        false)
                        .map(node -> node.data().data())
                        .collect(Collectors.toList()),
                StreamSupport.stream(
                        TreeTraverser.<Node<String>>using(Node::children)
                                .breadthFirstTraversal(stringTreeRoot).spliterator(),
                        false)
                        .map(Node::data)
                        .collect(Collectors.toList()));
        Assert.assertEquals(
                StreamSupport.stream(
                        TreeTraverser.<Node<LinkableData>>using(Node::children)
                                .breadthFirstTraversal(linkableTreeList.get(0)).spliterator(),
                        false)
                        .map(node -> node.data().id())
                        .collect(Collectors.toList()),
                StreamSupport.stream(
                        TreeTraverser.<Node<String>>using(Node::children)
                                .breadthFirstTraversal(stringTreeRoot).spliterator(),
                        false)
                        .map(Node::data)
                        .collect(Collectors.toList()));
        Assert.assertEquals(
                StreamSupport.stream(
                        TreeTraverser.<Node<Node<String>>>using(Node::children)
                                .preOrderTraversal(treeList.get(0)).spliterator(),
                        false)
                        .map(node -> node.data().data())
                        .collect(Collectors.toList()),
                StreamSupport.stream(
                        TreeTraverser.<Node<String>>using(Node::children)
                                .preOrderTraversal(stringTreeRoot).spliterator(),
                        false)
                        .map(Node::data)
                        .collect(Collectors.toList()));
        Assert.assertEquals(
                StreamSupport.stream(
                        TreeTraverser.<Node<LinkableData>>using(Node::children)
                                .preOrderTraversal(linkableTreeList.get(0)).spliterator(),
                        false)
                        .map(node -> node.data().id())
                        .collect(Collectors.toList()),
                StreamSupport.stream(
                        TreeTraverser.<Node<String>>using(Node::children)
                                .preOrderTraversal(stringTreeRoot).spliterator(),
                        false)
                        .map(Node::data)
                        .collect(Collectors.toList()));

        treeConstructor.clear();
        linkableDataTreeConstructor.clear();
        StreamSupport.stream(
                TreeTraverser.<Node<String>>using(Node::children)
                        .preOrderTraversal(stringTreeRoot).spliterator(),
                false)
                // skip root 'h'
                .skip(1)
                .forEach(node -> {
                    treeConstructor.add(new Node<>(node));
                    linkableDataTreeConstructor.add(new Node<>(
                            new LinkableData(node.data(), node.parent() == null ? "" : node.parent().data())));
                });
        Assert.assertEquals(
                treeConstructor.treeList()
                        .stream()
                        .map(tree ->
                                StreamSupport.stream(
                                        TreeTraverser.<Node<Node<String>>>using(Node::children)
                                                .breadthFirstTraversal(tree).spliterator(),
                                        false)
                                        .map(node -> node.data().data())
                                        .collect(Collectors.joining()))
                        .sorted()
                        .collect(Collectors.toList()),
                stringTreeRoot.children()
                        .stream()
                        .map(rootChild ->
                                StreamSupport.stream(
                                        TreeTraverser.<Node<String>>using(Node::children)
                                                .breadthFirstTraversal(rootChild).spliterator(),
                                        false)
                                        .map(Node::data)
                                        .collect(Collectors.joining()))
                        .sorted()
                        .collect(Collectors.toList()));
        Assert.assertEquals(
                linkableDataTreeConstructor.treeList()
                        .stream()
                        .map(tree ->
                                StreamSupport.stream(
                                        TreeTraverser.<Node<LinkableData>>using(Node::children)
                                                .breadthFirstTraversal(tree).spliterator(),
                                        false)
                                        .map(node -> node.data().id())
                                        .collect(Collectors.joining()))
                        .sorted()
                        .collect(Collectors.toList()),
                stringTreeRoot.children()
                        .stream()
                        .map(rootChild ->
                                StreamSupport.stream(
                                        TreeTraverser.<Node<String>>using(Node::children)
                                                .breadthFirstTraversal(rootChild).spliterator(),
                                        false)
                                        .map(Node::data)
                                        .collect(Collectors.joining()))
                        .sorted()
                        .collect(Collectors.toList()));
    }
}
