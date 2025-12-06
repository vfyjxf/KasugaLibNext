package lib.kasuga.slp.javet.converter;

import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.security.Api;
import lib.kasuga.scripting.value.ScriptFunction;
import lib.kasuga.scripting.value.ScriptValue;

@FunctionalInterface
public interface V8ScriptFunctionProxy {
    @Api
    ScriptValue apply(ScriptValue... args) throws ScriptException;
}
