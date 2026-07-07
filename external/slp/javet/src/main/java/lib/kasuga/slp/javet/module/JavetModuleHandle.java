package lib.kasuga.slp.javet.module;

import lib.kasuga.scripting.ScriptEngineType;
import lib.kasuga.scripting.module.ScriptModuleHandle;
import lib.kasuga.scripting.value.ScriptValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JavetModuleHandle implements ScriptModuleHandle {
    private final Map<String, ScriptValue> exports;
    private final String sourcePath;
    private final ScriptEngineType<?> engine;

    public JavetModuleHandle(Map<String, ScriptValue> exports, String sourcePath, ScriptEngineType<?> engine) {
        this.exports = new HashMap<>(exports);
        this.sourcePath = sourcePath;
        this.engine = engine;
    }

    @Override
    public ScriptValue getExport(String name) {
        return exports.get(name);
    }

    @Override
    public Set<String> getExportNames() {
        return Set.copyOf(exports.keySet());
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
