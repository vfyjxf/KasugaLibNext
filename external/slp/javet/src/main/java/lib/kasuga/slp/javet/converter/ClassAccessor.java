package lib.kasuga.slp.javet.converter;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.callback.IJavetDirectCallable;
import com.caoccao.javet.interop.callback.JavetCallbackContext;
import com.caoccao.javet.interop.callback.JavetCallbackType;
import com.caoccao.javet.interop.converters.IJavetConverter;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueObject;
import lib.kasuga.scripting.security.Api;
import lib.kasuga.scripting.value.ScriptValue;
import lib.kasuga.slp.javet.value.JavetValueBridge;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

public class ClassAccessor {
    private final V8Runtime runtime;
    private final IJavetConverter provider;
    private final Class<?> classType;
    Map<String, Function<V8Value[], V8Value>> quickAccessors = new HashMap<>();
    Map<String, MethodOverrideMap> methods = new HashMap<>();
    Map<String, Field> fields = new HashMap<>();

    boolean isFunctionalInterface = false;
    String functionalMethodName;

    public ClassAccessor(
            V8Runtime runtime,
            IJavetConverter provider,
            Class<?> classType
    ){
        this.runtime = runtime;
        this.provider = provider;
        this.classType = classType;
    }


    public Object invoke(
            Object target,
            String name,
            V8Value ...value
    ) throws InvocationTargetException, IllegalAccessException, JavetException {
        if(quickAccessors.containsKey(name)) {
            return quickAccessors.get(name).apply(value);
        }

        if(!methods.containsKey(name)) {
            throw new RuntimeException("Illegal invocation");
        }

        MethodOverrideMap overrideMap = methods.get(name);

        int valueSize = value == null ? 0 : value.length;
        BitSet convertMask = overrideMap.converterMask.get(valueSize);
        List<Method> localMethods = overrideMap.methods.get(valueSize);

        boolean varArgMethods = !overrideMap.varArgsMethods.isEmpty();
        if((convertMask == null || localMethods == null) && !varArgMethods) {
            throw new RuntimeException("Illegal invocation");
        }

        List<Method> consideringMethod = new ArrayList<>();
        if(localMethods != null) consideringMethod.addAll(localMethods);
        consideringMethod.addAll(overrideMap.varArgsMethods);

        Object[] arrayParameters = new Object[valueSize];

        for(int i = 0; i < valueSize ; i++) {
            if(varArgMethods || !convertMask.get(i)){
                arrayParameters[i] = JavetValueBridge.wrap(value[i]);
                continue;
            }
            Object nativeObject = provider.toObject(value[i]);
            arrayParameters[i] = nativeObject == null ?
                    value[i] : nativeObject;
        }

        ScriptValue[] values = new ScriptValue[valueSize];

        for(int methodIndex = 0; methodIndex < consideringMethod.size(); methodIndex++){
            Method localMethod = consideringMethod.get(methodIndex);
            Class<?>[] parameterTypes = localMethod.getParameterTypes();
            Parameter[] parameterInstances = localMethod.getParameters();
            Object[] parameters = new Object[valueSize];
            boolean isSignatureMatch = true;
            int incomeParameterIndex = valueSize - 1;
            boolean lastMatchVarargs = false;
            for (int acceptParameterIndex = parameterTypes.length - 1; acceptParameterIndex >= 0; acceptParameterIndex--) {
                if(lastMatchVarargs && incomeParameterIndex < 0) {
                    continue;
                }
                Class<?> parameterType = parameterTypes[acceptParameterIndex];
                if(parameterType == ScriptValue.class){
                    if(values[incomeParameterIndex] == null){
                        values[incomeParameterIndex] = JavetValueBridge.wrap(value[incomeParameterIndex]);
                    }
                    parameters[incomeParameterIndex] = values[incomeParameterIndex];

                    lastMatchVarargs = false;
                    if(parameterInstances[acceptParameterIndex].isVarArgs()){
                        acceptParameterIndex++;
                        lastMatchVarargs = true;
                    }
                    incomeParameterIndex--;

                    continue;
                }

                if(arrayParameters[incomeParameterIndex] == null && value[incomeParameterIndex] != null) {
                    Object nativeObject = provider.toObject(value[incomeParameterIndex]);
                    arrayParameters[incomeParameterIndex] = nativeObject == null ?
                            value[incomeParameterIndex] : nativeObject;
                }

                if(arrayParameters[incomeParameterIndex] == null) {
                    isSignatureMatch = false;
                    break;
                }

                Class<?> valueType = arrayParameters[incomeParameterIndex].getClass();

                if(parameterInstances[acceptParameterIndex].isVarArgs()){
                    parameterType = parameterType.componentType();
                }

                if(
                        parameterType == valueType ||
                        parameterType.isAssignableFrom(valueType)
                ) {
                    parameters[incomeParameterIndex] = arrayParameters[incomeParameterIndex];
                    lastMatchVarargs = false;
                    if(parameterInstances[acceptParameterIndex].isVarArgs()){
                        acceptParameterIndex++;
                        lastMatchVarargs = true;
                    }
                    incomeParameterIndex--;
                    continue;
                }
                if(lastMatchVarargs) {
                    lastMatchVarargs = false;
                    continue;
                }
                isSignatureMatch = false;
                break;
            }
            if(!isSignatureMatch){
                continue;
            }
            if(localMethod.isVarArgs()) {
                int totalParams = localMethod.getParameterCount();
                int totalNonVarargParams = totalParams - 1;
                Object[] varArgsParameter = new Object[totalParams];
                System.arraycopy(
                        parameters,
                        0,
                        varArgsParameter,
                        0,
                        localMethod.getParameterCount() - 1
                );
                Object[] varArgs = (Object[]) Array.newInstance(
                        localMethod.getParameterTypes()[totalNonVarargParams].getComponentType(),
                        valueSize - totalNonVarargParams
                );
                System.arraycopy(
                        parameters,
                        totalNonVarargParams,
                        varArgs,
                        0,
                        valueSize - totalNonVarargParams
                );
                varArgsParameter[totalNonVarargParams] = varArgs;
                parameters = varArgsParameter;
            }
            return localMethod.invoke(target, parameters);
        }
        throw new RuntimeException("Illegal invocation");
    }

    public Object get(Object object, String name) throws IllegalAccessException {
        return fields.get(name).get(object);
    }

    public Object set(
            Object object,
            String name,
            V8Value value
    ) throws JavetException, IllegalAccessException {
        Object nativeObject = provider.toObject(value);
        if(
                !fields.get(name).getType().isAssignableFrom(nativeObject.getClass()) &&
                !Modifier.isFinal(fields.get(name).getModifiers())
        ) {
            throw new RuntimeException("Illegal operation");
        }
        fields.get(name).set(object, nativeObject);
        return nativeObject;
    }

    public void addMethod(String name, Method method){
        MethodOverrideMap overrideMap = methods.computeIfAbsent(name, (i)->new MethodOverrideMap());
        overrideMap.initIfAbsent(method.getParameterCount());
        overrideMap.methods.get(method.getParameterCount()).add(method);
        Class<?>[] parameterTypes = method.getParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++) {
            if(parameterTypes[i] == ScriptValue.class){
                overrideMap.converterMask.get(method.getParameterCount()).set(i, false);
            }
        }

        if(method.getReturnType() != Void.class){
            overrideMap.isVoidReturn = false;
        }

        if(method.isVarArgs()) {
            overrideMap.varArgsMethods.add(method);
        }


    }

    public void addField(String name, Field field){
        this.fields.put(name, field);
    }

    public static ClassAccessor collect(
            V8Runtime runtime,
            IJavetConverter converter,
            Class<?> classType
    ){
        ClassAccessor accessor = new ClassAccessor(runtime, converter, classType);
        Method[] methods = classType.getMethods();
        HashSet<Method> filteredMethods = new HashSet<>();
        for (Method method : methods) {
            if(method.isAnnotationPresent(Api.class)){
                filteredMethods.add(method);
            }
        }
        Class<?>[] interfaces = classType.getInterfaces();
        Map<Method, Method> varArgsMethods = new HashMap<>();
        for (int i = 0; i < interfaces.length; i++) {
            Method[] interfaceMethods = interfaces[i].getDeclaredMethods();
            for (Method interfaceMethod : interfaceMethods) {
                for(Method method : methods){
                    if(
                            Arrays.equals(method.getTypeParameters(), interfaceMethod.getTypeParameters()) &&
                                    method.getReturnType() == interfaceMethod.getReturnType() &&
                                    Modifier.isAbstract(interfaceMethod.getModifiers())
                    ){
                        if(interfaces[i].isAnnotationPresent(FunctionalInterface.class)) {
                            filteredMethods.add(method);
                            accessor.isFunctionalInterface = true;
                            accessor.functionalMethodName = method.getName();
                        }
                        if(interfaceMethod.isVarArgs() && !method.isVarArgs()) {
                            varArgsMethods.put(method, interfaceMethod);
                        }
                    }
                }
            }
        }

        for (Method filteredMethod : filteredMethods) {
            if(varArgsMethods.containsKey(filteredMethod))
                filteredMethod = varArgsMethods.get(filteredMethod);
            filteredMethod.setAccessible(true);
            accessor.addMethod(filteredMethod.getName(), filteredMethod);
        }

        Field[] fields = classType.getFields();

        for (Field field : fields) {
            if(field.isAnnotationPresent(Api.class)){
                accessor.addField(field.getName(), field);
            }
        }

        return accessor;
    }

    public void bindPrototypeTo(V8Runtime runtime, FastJavetClassConverter converter, V8ValueObject value) throws JavetException {
        for (Map.Entry<String, MethodOverrideMap> entry : methods.entrySet()) {
            String name = entry.getKey();
            MethodOverrideMap overrideMap = entry.getValue();
            bind(runtime, value, converter, name, overrideMap.isVoidReturn);
        }

        for (Map.Entry<String, Field> fieldEntry : fields.entrySet()){
            String name = fieldEntry.getKey();
            Field field = fieldEntry.getValue();
            bindProp(runtime, value, converter, name, Modifier.isFinal(field.getModifiers()));
        }
    }

    protected void bind(
            V8Runtime runtime,
            V8ValueObject value,
            FastJavetClassConverter converter,
            String name,
            boolean voidReturn
    ) throws JavetException {
        System.out.printf("BIND RUNTIME: %s \n", name);
        JavetCallbackContext context = NativeProxyAccessor.getCallbackContext(
                runtime,
                converter,
                this,
                classType,
                name,
                voidReturn ?
                        NativeProxyAccessor.AccessorType.METHOD_VOID
                        : NativeProxyAccessor.AccessorType.METHOD
                );
        value.bindFunction(context);
    }


    protected void bindProp(
            V8Runtime runtime,
            V8ValueObject value,
            FastJavetClassConverter converter,
            String name,
            boolean isReadOnly
    ) throws JavetException {
        System.out.printf("BIND RUNTIME: %s \n", name);
        JavetCallbackContext readContext = NativeProxyAccessor.getCallbackContext(
                runtime,
                converter,
                this,
                classType,
                name,
                NativeProxyAccessor.AccessorType.FIELD_READ
        );
        if(isReadOnly){
            value.bindProperty(readContext);
            return;
        }
        JavetCallbackContext writeContext = NativeProxyAccessor.getCallbackContext(
                runtime,
                converter,
                this,
                classType,
                name,
                NativeProxyAccessor.AccessorType.FIELD_WRITE
        );
        value.bindProperty(readContext, writeContext);
    }

    public V8ValueObject createObject(
            Object object,
            IJavetConverter converter,
            V8Runtime v8Runtime
    ) throws JavetException {
        if(isFunctionalInterface){
            return v8Runtime.createV8ValueFunction(new JavetCallbackContext(
                    "apply",
                    JavetCallbackType.DirectCallNoThisAndResult,
                    (IJavetDirectCallable.NoThisAndResult) (V8Value ...values)->{
                        return converter.toV8Value(v8Runtime, invoke(object, functionalMethodName, values));
                    }
            ));
        }
        return v8Runtime.createV8ValueObject();
    }
}
