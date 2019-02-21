package tech.geekcity.open.geek.tools.tree;

import com.google.common.collect.TreeTraverser;
import org.apache.commons.lang3.StringUtils;
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
    private Node<String> root;

    @Before
    public void before() throws Exception {
        root = new Node<>("h")
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
                        return StringUtils.equals(
                                node.data().data(),
                                anotherNode.data().data());
                    }
                });
        TreeTraverser.<Node<String>>using(Node::children)
                .postOrderTraversal(root)
                .forEach(node -> treeConstructor.add(new Node<>(node)));
        List<Node<Node<String>>> treeList = treeConstructor.treeList();
        Assert.assertEquals(1, treeList.size());
        Assert.assertEquals(
                StreamSupport.stream(
                        TreeTraverser.<Node<Node<String>>>using(Node::children)
                                .breadthFirstTraversal(treeList.get(0)).spliterator(),
                        false)
                        .map(node -> node.data().data())
                        .collect(Collectors.toList()),
                StreamSupport.stream(
                        TreeTraverser.<Node<String>>using(Node::children)
                                .breadthFirstTraversal(root).spliterator(),
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
                                .preOrderTraversal(root).spliterator(),
                        false)
                        .map(Node::data)
                        .collect(Collectors.toList()));

        treeConstructor.clear();
        StreamSupport.stream(
                TreeTraverser.<Node<String>>using(Node::children)
                        .preOrderTraversal(root).spliterator(),
                false)
                // skip root 'h'
                .skip(1)
                .forEach(node -> treeConstructor.add(new Node<>(node)));
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
                root.children()
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
