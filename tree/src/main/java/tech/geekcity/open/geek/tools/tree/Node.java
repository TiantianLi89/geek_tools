package tech.geekcity.open.geek.tools.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author ben.wangz
 */
public class Node<DataType extends Serializable> implements Serializable {
    private DataType data;
    private Node<DataType> parent;
    private List<Node<DataType>> children;

    public Node(DataType data) {
        this.data = data;
        this.parent = null;
        this.children = new ArrayList<>();
    }

    public Node addChild(Node<DataType> child) {
        children.add(child);
        child.parent = this;
        return this;
    }

    public boolean root() {
        return null == parent;
    }

    public Node<DataType> parent() {
        return parent;
    }

    public List<Node<DataType>> children() {
        return children;
    }

    public DataType data() {
        return data;
    }

    public Iterator<Node<DataType>> parentNodeIterator() {
        return new Iterator<Node<DataType>>() {
            private Node<DataType> currentNode = Node.this;

            @Override
            public boolean hasNext() {
                boolean hasNext = !currentNode.root();
                currentNode = currentNode.parent();
                return hasNext;
            }

            @Override
            public Node<DataType> next() {
                return currentNode;
            }
        };
    }
}