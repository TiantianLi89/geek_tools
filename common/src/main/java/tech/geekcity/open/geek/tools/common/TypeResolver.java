package tech.geekcity.open.geek.tools.common;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TypeResolver<BaseClass> extends TypeIdResolverBase {
    private JavaType baseType;

    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;
        super.init(baseType);
    }

    @Override
    public String idFromValue(Object value) {
        return idFromValueAndType(value, value.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        List<Class<? extends BaseClass>> matchedClassList = supportedClasses().stream()
                .filter(clazz -> clazz.isAssignableFrom(suggestedType))
                .collect(Collectors.toList());
        if (matchedClassList.size() > 1) {
            String allMatchedClasses = StringUtils.join(
                    matchedClassList.stream()
                            .map(Class::getName)
                            .collect(Collectors.toList()),
                    ",");
            throw new RuntimeException(
                    String.format("more than one supported class(%s) found for(%s)",
                            allMatchedClasses, suggestedType.getName()));
        }
        if (matchedClassList.size() < 1) {
            String allSupportedClasses = StringUtils.join(
                    supportedClasses().stream()
                            .map(Class::getName)
                            .collect(Collectors.toList()),
                    ",");
            throw new RuntimeException(
                    String.format("not supported class: (%s) is not one of (%s)",
                            allSupportedClasses, suggestedType.getName()));
        }
        return matchedClassList.get(0).getName();
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) throws IOException {
        try {
            return context.constructSpecializedType(baseType, Class.forName(id));
        } catch (ClassNotFoundException ex) {
            throw new IOException(
                    String.format("class(%s) not found: %s", id, ex.getMessage()),
                    ex);
        }
    }

    protected abstract List<Class<? extends BaseClass>> supportedClasses();
}