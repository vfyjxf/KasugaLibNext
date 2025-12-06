package lib.kasuga.slp.javet.value;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.IV8Value;
import com.caoccao.javet.values.IV8ValuePrimitiveObject;
import com.caoccao.javet.values.primitive.V8ValueNull;
import com.caoccao.javet.values.primitive.V8ValuePrimitive;
import com.caoccao.javet.values.primitive.V8ValueUndefined;
import com.caoccao.javet.values.reference.V8ValueFunction;
import com.caoccao.javet.values.reference.V8ValueObject;
import com.caoccao.javet.values.reference.V8ValuePromise;
import lib.kasuga.scripting.value.ScriptValue;

public class JavetValueBridge {
    public static ScriptValue wrap(IV8Value value) throws JavetException {
        if(value instanceof V8ValuePrimitive<?> primitiveValue) {
            return new JavetValuePrimitive(primitiveValue);
        }

        if(value instanceof IV8ValuePrimitiveObject<?> && value instanceof V8ValueObject object) {
            return new JavetValuePrimitiveObject(object);
        }

        if(value instanceof V8ValueFunction function) {
            return new JavetValueFunction(function);
        }

        if(value instanceof V8ValueObject object) {
            return new JavetValueObject<>(object);
        }

        if(value instanceof V8ValueUndefined || value instanceof V8ValueNull) {
            return null;
        }

        if(!value.isClosed())
            value.close();

        throw new IllegalArgumentException("Unknown type: " + value.getClass().getName() + ", cannot convert!");
    }
}
