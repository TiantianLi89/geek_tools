package tech.geekcity.open.geek.tools.tree;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>
 * any DataType implements this interface should satisfy that
 * NodeComparator can have different implementations
 * but should behave the same with {@link LinkableDataType#nodeComparator()}
 * </p>
 *
 * @author ben.wangz
 */

public interface LinkableDataType extends Serializable {
    String id();

    String parentId();

    static <DataType extends LinkableDataType> TreeConstructor.NodeComparator<DataType> nodeComparator() {
        return new TreeConstructor.NodeComparator<DataType>() {
            @Override
            public boolean isParent(Node<DataType> parent, Node<DataType> child) {
                return StringUtils.equals(
                        child.data().parentId(),
                        parent.data().id());
            }

            @Override
            public boolean equals(Node<DataType> node, Node<DataType> anotherNode) {
                return Objects.equals(node.data(), anotherNode.data());
            }
        };
    }
}
