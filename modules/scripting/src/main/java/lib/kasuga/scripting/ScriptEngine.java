package lib.kasuga.scripting;

import lib.kasuga.scripting.value.ScriptValue;

public interface ScriptEngine {
    public void init(ScriptConsole console) throws ScriptException;
    public ScriptValue createValue(Object object) throws ScriptException;
}
