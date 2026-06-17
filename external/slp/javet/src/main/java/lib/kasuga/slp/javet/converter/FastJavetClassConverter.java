package lib.kasuga.slp.javet.converter;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.callback.IJavetDirectCallable;
import com.caoccao.javet.interop.callback.JavetCallbackContext;
import com.caoccao.javet.interop.callback.JavetCallbackType;
import com.caoccao.javet.interop.converters.JavetObjectConverter;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueObject;
import lib.kasuga.scripting.ScriptEngine;
import lib.kasuga.scripting.security.SecurityEngineFeature;
import lib.kasuga.scripting.value.ScriptValue;
import lib.kasuga.slp.javet.value.JavetValue;
import lib.kasuga.slp.javet.value.JavetValueBridge;
import lombok.Setter;

import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;

public class FastJavetClassConverter extends JavetObjectConverter {
    private final V8Runtime v8Runtime;
    private final ScriptConverter scriptConverter;
    @Setter
    private SecurityEngineFeature securityFeature;
    @Setter
    private ScriptEngine engine;

    public FastJavetClassConverter(V8Runtime v8Runtime){
        this.v8Runtime = v8Runtime;
        scriptConverter = new ScriptConverter(this);
    }
    public ConcurrentHashMap<Class<?>, V8ValueObject> classTypeCache = new ConcurrentHashMap<>();

    public ConcurrentHashMap<Class<?>, ClassAccessor> accessors = new ConcurrentHashMap<>();


    public V8ValueObject getClassPrototype(Class<?> prototype) throws JavetException {
        if(classTypeCache.containsKey(prototype))
            return classTypeCache.get(prototype);
        V8ValueObject prototypeObject = v8Runtime.createV8ValueObject();

        ClassAccessor accessor = ClassAccessor.collect(v8Runtime, this, prototype);
        accessor.setSecurityFeature(securityFeature);
        accessor.setEngine(engine);
        accessors.put(prototype, accessor);

        accessor.bindPrototypeTo(v8Runtime, this, prototypeObject);

        classTypeCache.put(prototype, prototypeObject);

        return prototypeObject;
    }


    ConcurrentHashMap<Integer, WeakReference<Object>> cachedObjects = new ConcurrentHashMap<>();

    ConcurrentHashMap<Long, Integer> trackingObjects = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends V8Value> T toV8Value(V8Runtime v8Runtime, Object object, int depth) throws JavetException {
        switch (object) {
            case null -> {
                return (T) v8Runtime.createV8ValueNull();
            }
            case JavetValue<?> value -> {
                return value.getDelegate().toClone();
            }
            case ScriptValue scriptValue -> {
                return this.scriptConverter.toV8Value(v8Runtime, scriptValue, depth);
            }
            case V8Value value -> {
                return value.toClone();
            }
            default -> {
            }
        }

        if(
                object instanceof int[] ||
                        object instanceof float[] ||
                        object instanceof double[] ||
                        object instanceof long[] ||
                        object instanceof short[] ||
                        object instanceof byte[] ||
                        object instanceof String ||
                        object instanceof Boolean ||
                        object instanceof Byte ||
                        object instanceof Short ||
                        object instanceof Integer ||
                        object instanceof Long ||
                        object instanceof Float ||
                        object instanceof Double ||
                        object instanceof Character ||
                        object instanceof BigInteger
        ){
            // Directly convert to V8Value, no need to process
            return super.toV8Value(v8Runtime, object, depth);
        }
        T v8Value = super.toV8Value(v8Runtime, object, depth);

        if (v8Value != null && !(v8Value.isUndefined())) {
            if(v8Value instanceof V8ValueObject v8ValueObject){
                attachGcTracker(v8ValueObject, object);
            }
            return v8Value;
        }

        V8ValueObject proto = this.getClassPrototype(object.getClass());

        V8ValueObject childObject = this.accessors.get(object.getClass()).createObject(object, this, v8Runtime);
        attachGcTracker(childObject, object);
        childObject.set("__proto__",proto);
        return (T) childObject;
    }

    private void attachGcTracker(V8ValueObject v8ValueObject, Object object) throws JavetException {
        int hashCode = System.identityHashCode(object);
        cachedObjects.put(hashCode, new WeakReference<>(object));
        v8ValueObject.setPrivateProperty("KasugaLib#Address", hashCode);
        v8ValueObject.bindFunction(new JavetCallbackContext(
                "__nGCTracker__",
                JavetCallbackType.DirectCallNoThisAndResult,
                (IJavetDirectCallable.NoThisAndResult<?>) (V8Value ...value) -> {
                    return toV8Value(v8Runtime, object);
                }
        ));
        v8ValueObject.setWeak();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T toObject(V8Value v8Value, int depth) throws JavetException {
        if(v8Value instanceof V8ValueObject object){
            if(object.hasPrivateProperty("KasugaLib#Address")){
                int address = object.getPrivatePropertyInteger("KasugaLib#Address");
                if(cachedObjects.containsKey(address)){
                    Object nativeObject = cachedObjects.get(address).get();
                    return (T) nativeObject;
                }
            }

        }
        T parentConvertResult = super.toObject(v8Value, depth);
        if(parentConvertResult instanceof V8Value){
            return (T) JavetValueBridge.wrap(v8Value);
        }
        return parentConvertResult;
    }

    public Object getNativeObject(V8ValueObject object) throws JavetException {
        Object idObj = object.getPrivatePropertyPrimitive("KasugaLib#Address");
        if(!(idObj instanceof Integer id)){
            throw new RuntimeException("Invalid Innvocation");
        }
        return cachedObjects.get(id).get();
    }

    public int getObjectCount() {
        int size = 0;
        for (WeakReference<Object> value : cachedObjects.values()) {
            if(value.get() != null)
                size ++;
        }
        return size;
    }
}
