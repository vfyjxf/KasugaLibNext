package lib.kasuga.slp.javet.module;

import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.module.ScriptModuleHandle;

@FunctionalInterface
public interface RequireResolver {
    ScriptModuleHandle resolve(String moduleName, String fromSourcePath) throws ScriptException;
}
