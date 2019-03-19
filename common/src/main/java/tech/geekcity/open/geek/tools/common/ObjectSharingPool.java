package tech.geekcity.open.geek.tools.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ben.wangz
 */
public class ObjectSharingPool {
    private final Map<String, Object> pool = new HashMap<>();

    public boolean contains(Class clazz) {
        return pool.containsKey(clazz.getName());
    }

    public void store(Object object, boolean override) {
        pool.merge(
                object.getClass().getName(),
                object,
                (oldValue, newValue) -> override ? newValue : oldValue);
    }

    public Object instance(Class clazz) {
        return pool.get(clazz.getName());
    }
}
