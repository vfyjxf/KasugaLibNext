package lib.kasuga.scripting.value;

import lib.kasuga.scripting.ScriptException;

public interface ScriptArray extends ScriptValue {
    public ScriptValue[] asArray() throws ScriptException;

    @Override
    ScriptArray cloneValue() throws ScriptException;
}
