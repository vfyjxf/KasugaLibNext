package lib.kasuga.scripting;

import jakarta.annotation.Nullable;
import lib.kasuga.scripting.module.ResolvedScript;
import lib.kasuga.scripting.module.ScriptModuleHandle;
import lib.kasuga.scripting.value.ScriptValue;

import java.io.InputStream;

public interface ScriptEngine {
    void init(ScriptConsole console) throws ScriptException;
    ScriptValue createValue(Object object) throws ScriptException;

    ScriptEngineType<?> getType();

    ScriptModuleHandle loadModule(ResolvedScript script) throws ScriptException;

    @Nullable
    ScriptModuleHandle getLoadedModule(String sourcePath);

    void executeEntry(String entryName, InputStream source) throws ScriptException;

    void tick();

    void close();
}
