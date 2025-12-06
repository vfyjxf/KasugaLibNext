package lib.kasuga.slp.javet.converter;

import com.caoccao.javet.exceptions.JavetError;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.converters.JavetObjectConverter;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueArray;
import com.caoccao.javet.values.reference.V8ValueObject;
import io.netty.util.internal.ResourcesUtil;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.value.*;

public class ScriptConverter  {
    protected final FastJavetClassConverter root;

    public ScriptConverter(FastJavetClassConverter root) {
        this.root = root;
    }

    public <T extends V8Value> T toV8Value(V8Runtime v8Runtime, ScriptValue scriptValue, int depth) throws JavetException {
        if(scriptValue instanceof ScriptPrimitive primitive) {
            try {
                return root.toV8Value(v8Runtime, primitive.getValue(), depth + 1);
            } catch (ScriptException e) {
                throw new JavetException(JavetError.ConverterFailure, e);
            }
        } else if(scriptValue instanceof ScriptObject originalObj) {
            V8ValueObject object = null;
            try {
                object = v8Runtime.createV8ValueObject();
                for (ScriptValue objectKey : originalObj.getObjectKeys()) {
                    V8ValueObject convertedKey = null, convertedValue = null;
                    ScriptValue value = null;
                    try{
                        convertedKey = root.toV8Value(v8Runtime, objectKey, depth + 1);
                        value = originalObj.getMember(objectKey);
                        convertedValue = root.toV8Value(v8Runtime, value, depth + 1);
                        object.setProperty(convertedKey.toString(), convertedValue);
                    } finally {
                        JavetResourceUtils.safeClose(objectKey, convertedValue);
                        if(value != null) value.close();
                    }
                }
            } catch (ScriptException e) {
                throw new JavetException(JavetError.ConverterFailure, e);
            } finally {
                JavetResourceUtils.safeClose(object);
            }
        } else if(scriptValue instanceof ScriptFunction function) {
            return root.toV8Value(v8Runtime, (V8ScriptFunctionProxy)(function::execute), depth + 1);
        } else if(scriptValue instanceof ScriptArray array) {
            V8ValueArray object = null;
            ScriptValue[] arrayUnpacked = null;
            try {
                object = v8Runtime.createV8ValueArray();
                arrayUnpacked = array.asArray();
                for (int i = 0; i < arrayUnpacked.length; i++) {
                    V8Value convertedValue = null;
                    ScriptValue value = null;
                    try {
                        value = arrayUnpacked[i];
                        convertedValue = root.toV8Value(v8Runtime, value, depth + 1);
                        object.push(convertedValue);
                    } finally {
                        JavetResourceUtils.safeClose(convertedValue);
                        if(value != null) value.close();
                    }
                }
            } catch (ScriptException e) {
                throw new JavetException(JavetError.ConverterFailure, e);
            } finally {
                JavetResourceUtils.safeClose((Object) arrayUnpacked);
            }
        }
        throw new JavetException(JavetError.ConverterFailure, new IllegalArgumentException("Unsupported ScriptValue type: " + scriptValue.getClass().getName()));
    }
}
