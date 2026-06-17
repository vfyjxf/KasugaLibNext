package lib.kasuga.slp.javet.value;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueArray;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.value.ScriptArray;
import lib.kasuga.scripting.value.ScriptValue;

public class JavetValueArray extends JavetValueObject<V8ValueArray> implements ScriptArray {
    public JavetValueArray(V8ValueArray array) {
        super(array);
    }

    @Override
    public ScriptValue[] asArray() throws ScriptException {
        V8Value[] unwrappedValues = null;
        try{
            ScriptValue[] values = new ScriptValue[delegate.getLength()];
            unwrappedValues = new V8Value[values.length];
            for (int i = 0; i < values.length; i++) {
                V8Value value = delegate.get(i);
                unwrappedValues[i] = value;
                values[i] = JavetValueBridge.wrap(value);
            }
            return values;
        } catch (JavetException t) {
            JavetResourceUtils.safeClose((Object) unwrappedValues);
            throw new ScriptException(t);
        }
    }

    @Override
    public JavetValueArray cloneValue() throws ScriptException {
        return new JavetValueArray(cloneReference());
    }
}
