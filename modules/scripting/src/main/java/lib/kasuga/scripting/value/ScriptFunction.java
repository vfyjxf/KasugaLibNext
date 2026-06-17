package lib.kasuga.scripting.value;

import lib.kasuga.scripting.ScriptException;

public interface ScriptFunction extends ScriptValue {
    public ScriptValue execute(ScriptValue ...arguments) throws ScriptException;
    public void executeVoid(ScriptValue ...arguments) throws ScriptException;

    @Override
    ScriptFunction cloneValue() throws ScriptException;
}
