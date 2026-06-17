package lib.kasuga.slp.javet.value;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.IV8Value;
import com.caoccao.javet.values.IV8ValuePrimitiveObject;
import com.caoccao.javet.values.IV8ValuePrimitiveValue;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.primitive.V8ValuePrimitive;
import com.caoccao.javet.values.reference.V8ValueObject;
import com.caoccao.javet.values.reference.V8ValueSymbol;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.value.ScriptPrimitive;

public class JavetValuePrimitiveObject extends JavetValueObject<V8ValueObject> implements ScriptPrimitive {
    private final IV8ValuePrimitiveObject<?> primitive;

    public JavetValuePrimitiveObject(V8ValueObject delegate) {
        super(delegate);
        if(delegate instanceof IV8ValuePrimitiveObject<?> primitiveObject) {
            this.primitive = primitiveObject;
        } else {
            throw new IllegalArgumentException("Delegate is not a primitive object: " + delegate.getClass().getName());
        }
    }

    @Override
    public double asDouble() throws ScriptException {
        try {
            return primitive.asDouble();
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public int asInt() throws ScriptException {
        try {
            return primitive.asInt();
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public long asLong() throws ScriptException {
        try {
            return primitive.asLong();
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public short asShort() throws ScriptException {
        try {
            return (short) primitive.asInt();
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public byte asByte() throws ScriptException {
        try {
            return (byte) primitive.asInt();
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public Object getValue() throws ScriptException {
        try {
            V8Value val = primitive.valueOf();
            return this.delegate.checkV8Runtime().toObject(val);
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public JavetValuePrimitiveObject cloneValue() throws ScriptException {
        return new JavetValuePrimitiveObject(cloneReference());
    }
}
