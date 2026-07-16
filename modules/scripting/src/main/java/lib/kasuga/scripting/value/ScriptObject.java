package lib.kasuga.scripting.value;

import lib.kasuga.scripting.ScriptException;

public interface ScriptObject extends ScriptValue {
    public ScriptValue getMember(ScriptValue key) throws ScriptException;
    public ScriptValue setMember(ScriptValue key, ScriptValue member) throws ScriptException;
    public ScriptValue[] getObjectKeys() throws ScriptException;
    public void remove(ScriptValue key) throws ScriptException;

    @Override
    ScriptObject cloneValue() throws ScriptException;
}
