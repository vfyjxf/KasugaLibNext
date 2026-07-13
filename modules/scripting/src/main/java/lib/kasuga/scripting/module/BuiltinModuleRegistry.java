package lib.kasuga.scripting.module;

import io.micronaut.context.annotation.Context;
import lib.kasuga.scripting.ScriptEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Context()
public class BuiltinModuleRegistry {

    private final Map<String, Supplier<Object>> modules = new HashMap<>();
    private final Map<String, ScriptModuleFactory<?>> moduleFactories = new HashMap<>();

    public void register(String moduleName, Supplier<Object> supplier) {
        modules.put(moduleName, supplier);
    }

    public <M extends ScriptModule> void registerFactory(ScriptModuleFactory<M> factory) {
        moduleFactories.put(factory.name(), factory);
    }

    public Object resolve(String moduleName) {
        Supplier<Object> supplier = modules.get(moduleName);
        return supplier != null ? supplier.get() : null;
    }

    public Map<String, ScriptModule> createModules(ScriptEngine engine) {
        Map<String, ScriptModule> result = new HashMap<>();
        for (Map.Entry<String, ScriptModuleFactory<?>> entry : moduleFactories.entrySet()) {
            result.put(entry.getKey(), entry.getValue().create(engine));
        }
        return result;
    }

    public boolean has(String moduleName) {
        return modules.containsKey(moduleName);
    }

    public boolean hasFactory(String moduleName) {
        return moduleFactories.containsKey(moduleName);
    }

    public Set<String> names() {
        return Set.copyOf(modules.keySet());
    }

    public Set<String> factoryNames() {
        return Set.copyOf(moduleFactories.keySet());
    }

    public void clear() {
        modules.clear();
        moduleFactories.clear();
    }
}
