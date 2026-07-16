package lib.kasuga.scripting.module;

import lib.kasuga.scripting.ScriptEngine;

public interface ScriptModuleFactory<M extends ScriptModule> {
    String name();
    M create(ScriptEngine engine);
}
