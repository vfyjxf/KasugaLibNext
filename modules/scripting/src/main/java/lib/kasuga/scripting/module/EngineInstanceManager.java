package lib.kasuga.scripting.module;

import io.micronaut.context.annotation.Context;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import lib.kasuga.scripting.ScriptConsole;
import lib.kasuga.scripting.ScriptEngine;
import lib.kasuga.scripting.ScriptEngineRegistry;
import lib.kasuga.scripting.ScriptEngineType;

import java.util.HashMap;
import java.util.Map;

@Context()
public class EngineInstanceManager {

    @Inject()
    ScriptEngineRegistry engineRegistry;

    private final Map<ScriptEngineType<?>, ScriptEngine> sharedInstances = new HashMap<>();

    public ScriptEngine getOrCreate(ScriptEngineType<?> type, @Nullable ScriptEngine currentEngine) {
        if (currentEngine != null && currentEngine.getType() == type) {
            return currentEngine;
        }
        return sharedInstances.computeIfAbsent(type, t -> {
            ScriptEngine engine = t.engineSupplier.get();
            try {
                engine.init(new ScriptConsole() {
                    @Override public void log(String message) {}
                    @Override public void warn(String message) {}
                    @Override public void debug(String message) {}
                    @Override public void info(String message) {}
                    @Override public void error(String message) {}
                });
            } catch (Exception e) {
                throw new RuntimeException("Failed to init engine: " + t.scriptType, e);
            }
            return engine;
        });
    }

    public void clear() {
        sharedInstances.values().forEach(engine -> {
            try {
                engine.close();
            } catch (Exception ignored) {}
        });
        sharedInstances.clear();
    }
}
