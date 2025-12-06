package lib.kasuga.scripting.value;

import lib.kasuga.scripting.ScriptException;

public interface ScriptValue {
    public String asString() throws ScriptException;

    public void close() throws ScriptException;
}
