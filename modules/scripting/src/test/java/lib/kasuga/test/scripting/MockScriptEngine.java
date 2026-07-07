package lib.kasuga.test.scripting;

import jakarta.annotation.Nullable;
import lib.kasuga.scripting.*;
import lib.kasuga.scripting.feature.EngineFeature;
import lib.kasuga.scripting.feature.EngineFeatureType;
import lib.kasuga.scripting.module.ResolvedScript;
import lib.kasuga.scripting.module.ScriptModuleHandle;
import lib.kasuga.scripting.value.ScriptValue;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MockScriptEngine implements ScriptEngine {
    private ScriptEngineType<?> type;
    private final Map<String, ScriptModuleHandle> loadedModules = new HashMap<>();
    private Map<EngineFeatureType<?>, EngineFeature> features = Map.of();
    private ScriptConsole console;

    public MockScriptEngine() {
    }

    @Override
    public void init(ScriptConsole console) throws ScriptException {
        this.console = console;
    }

    @Override
    public void setFeatures(Map<EngineFeatureType<?>, EngineFeature> features) {
        this.features = features;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <F extends EngineFeature> F getFeature(EngineFeatureType<F> type) {
        return (F) features.get(type);
    }

    @Override
    public ScriptValue createValue(Object object) throws ScriptException {
        return new ScriptValue() {
            @Override
            public String asString() { return object.toString(); }
            @Override
            public void close() {}
        };
    }

    @Override
    public ScriptEngineType<?> getType() {
        return type;
    }

    public void setType(ScriptEngineType<?> type) {
        this.type = type;
    }

    @Override
    public ScriptModuleHandle loadModule(ResolvedScript script) throws ScriptException {
        String sourcePath = script.filePath();
        ScriptModuleHandle handle = new MockScriptModuleHandle(sourcePath, type);
        loadedModules.put(sourcePath, handle);
        return handle;
    }

    @Override
    @Nullable
    public ScriptModuleHandle getLoadedModule(String sourcePath) {
        return loadedModules.get(sourcePath);
    }

    @Override
    public void executeEntry(String entryName, InputStream source) throws ScriptException {
        // no-op for mock
    }

    @Override
    public void tick() {
        // no-op for mock
    }

    @Override
    public void close() {
        loadedModules.clear();
    }

    private static class MockScriptModuleHandle implements ScriptModuleHandle {
        private final String sourcePath;
        private final ScriptEngineType<?> engine;

        MockScriptModuleHandle(String sourcePath, ScriptEngineType<?> engine) {
            this.sourcePath = sourcePath;
            this.engine = engine;
        }

        @Override
        public ScriptValue getExport(String name) {
            return new ScriptValue() {
                @Override
                public String asString() { return "mock:" + name; }
                @Override
                public void close() {}
            };
        }

        @Override
        public Set<String> getExportNames() {
            return Set.of();
        }

        @Override
        public ScriptEngineType<?> getEngine() {
            return engine;
        }

        @Override
        public String getSourcePath() {
            return sourcePath;
        }
    }
}
