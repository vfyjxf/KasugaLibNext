package lib.kasuga.scripting.module;

import lib.kasuga.scripting.ScriptEngineType;
import lib.kasuga.scripting.value.ScriptValue;

import java.util.Set;

public interface ScriptModuleHandle {

    ScriptValue getExport(String name);

    Set<String> getExportNames();

    ScriptEngineType<?> getEngine();

    String getSourcePath();
}
