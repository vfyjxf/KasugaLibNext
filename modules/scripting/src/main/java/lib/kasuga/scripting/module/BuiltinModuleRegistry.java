package lib.kasuga.scripting.module;

import io.micronaut.context.annotation.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Context()
public class BuiltinModuleRegistry {

    private final Map<String, Supplier<Object>> modules = new HashMap<>();

    public void register(String moduleName, Supplier<Object> supplier) {
        modules.put(moduleName, supplier);
    }

    public Object resolve(String moduleName) {
        Supplier<Object> supplier = modules.get(moduleName);
        return supplier != null ? supplier.get() : null;
    }

    public boolean has(String moduleName) {
        return modules.containsKey(moduleName);
    }

    public Set<String> names() {
        return Set.copyOf(modules.keySet());
    }

    public void clear() {
        modules.clear();
    }
}
