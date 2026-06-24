package lib.kasuga.registration.data_driven;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TypeHandlerRegistry {

    private static final Map<String, TypeHandler<?>> HANDLERS = new LinkedHashMap<>();

    public static <T> void register(TypeHandler<T> handler) {
        HANDLERS.put(handler.getTypeName(), handler);
    }

    public static TypeHandler<?> get(String typeName) {
        return HANDLERS.get(typeName);
    }

    public static Collection<TypeHandler<?>> all() {
        return HANDLERS.values();
    }

    public static TypeHandler<?> findByParent(String parentType) {
        for (TypeHandler<?> handler : HANDLERS.values()) {
            if (parentType.equals(handler.getParentTypeName())) {
                return handler;
            }
        }
        return null;
    }

    private TypeHandlerRegistry() {}
}
