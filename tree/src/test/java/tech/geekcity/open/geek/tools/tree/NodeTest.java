package tech.geekcity.open.geek.tools.tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.TreeTraverser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * @author ben.wangz
 */
public class NodeTest {
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

    @After
    public void after() throws Exception {

    }

    @Test
    public void test() {
        TreeTraverser<Node<String>> treeTraverser = TreeTraverser.using(Node::children);
        List<String> breadthFirstDataList = StreamSupport
                .stream(
                        treeTraverser.breadthFirstTraversal(root).spliterator(),
                        false)
                .map(Node::data)
                .collect(Collectors.toList());
        Assert.assertEquals(ImmutableList.of("h", "d", "e", "g", "a", "b", "c", "f"), breadthFirstDataList);
        List<String> preOrderDataList = StreamSupport
                .stream(
                        treeTraverser.preOrderTraversal(root).spliterator(),
                        false)
                .map(Node::data)
                .collect(Collectors.toList());
        Assert.assertEquals(ImmutableList.of("h", "d", "a", "b", "c", "e", "g", "f"), preOrderDataList);
        List<String> postOrderDataList = StreamSupport
                .stream(
                        treeTraverser.postOrderTraversal(root).spliterator(),
                        false)
                .map(Node::data)
                .collect(Collectors.toList());
        Assert.assertEquals(ImmutableList.of("a", "b", "c", "d", "e", "f", "g", "h"), postOrderDataList);

        StreamSupport
                .stream(
                        treeTraverser.postOrderTraversal(root).spliterator(),
                        false)
                .forEach(node -> {
                    List<Node<String>> parentNodeDataList = StreamSupport
                            .stream(
                                    Spliterators.spliteratorUnknownSize(node.parentNodeIterator(), Spliterator.NONNULL),
                                    false)
                            .collect(Collectors.toList());
                    Assert.assertTrue(
                            IntStream.range(0, parentNodeDataList.size() - 1)
                                    .allMatch(index
                                            -> Objects.equals(
                                            parentNodeDataList.get(index).parent(),
                                            parentNodeDataList.get(index + 1))));
                });
    }
}
