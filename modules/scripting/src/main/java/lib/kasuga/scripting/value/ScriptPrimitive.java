package lib.kasuga.scripting.value;

import lib.kasuga.scripting.ScriptException;

public interface ScriptPrimitive extends ScriptValue {
    public double asDouble() throws ScriptException;
    public int asInt() throws ScriptException;
    public long asLong() throws ScriptException;
    public short asShort() throws ScriptException;
    public byte asByte() throws ScriptException;

    public Object getValue() throws ScriptException;
    @Override
    ScriptPrimitive cloneValue() throws ScriptException;
}
