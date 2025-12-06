package lib.kasuga.slp.javet.value;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.primitive.V8ValuePrimitive;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.value.ScriptPrimitive;

public class JavetValuePrimitive extends JavetValue<V8ValuePrimitive<?>> implements ScriptPrimitive {
    public JavetValuePrimitive(V8ValuePrimitive<?> delegate) {
        super(delegate);
    }

    @Override
    public double asDouble() throws ScriptException {
        try {
            return delegate.asDouble();
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public int asInt() throws ScriptException {
        try {
            return delegate.asInt();
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public long asLong() throws ScriptException {
        try {
            return delegate.asLong();
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public short asShort() throws ScriptException {
        try {
            return (short) delegate.asInt();
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public byte asByte() throws ScriptException {
        try {
            return (byte) delegate.asInt();
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public Object getValue() {
        return delegate.getValue();
    }


}
