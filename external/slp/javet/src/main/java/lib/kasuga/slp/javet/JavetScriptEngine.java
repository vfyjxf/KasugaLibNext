package lib.kasuga.slp.javet;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.utils.V8ValueUtils;
import lib.kasuga.scripting.ScriptConsole;
import lib.kasuga.scripting.ScriptEngine;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.value.ScriptValue;
import lib.kasuga.slp.javet.converter.FastJavetClassConverter;
import lib.kasuga.slp.javet.value.JavetValueBridge;
import lombok.Getter;


public class JavetScriptEngine implements ScriptEngine {
    @Getter V8Runtime runtime;
    private FastJavetClassConverter converter;

    public JavetScriptEngine() {}

    @Override
    public void init(ScriptConsole console) throws ScriptException {
        try {
            runtime = V8Host.getV8Instance().createV8Runtime();
            KasugaJavetConsoleInterceptor consoleInterceptor = new KasugaJavetConsoleInterceptor(runtime, console);
            consoleInterceptor.register(runtime.getGlobalObject());
            this.converter = new FastJavetClassConverter(runtime);
            runtime.setConverter(converter);
            runtime.setPromiseRejectCallback((event, promise, value)->{
                if(event.getCode() == 0){
                    console.error("Unhandled Promise Rejection: " + V8ValueUtils.asString(value));
                }
            });
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    public ScriptValue execute(String evaluateString) throws ScriptException {
        try{
            return JavetValueBridge.wrap(this.runtime.getExecutor(evaluateString).execute());
        }catch (JavetException e){
            throw new ScriptException(e);
        }
    }

    @Override
    public ScriptValue createValue(Object object) throws ScriptException {
        try {
            return JavetValueBridge.wrap(runtime.getConverter().toV8Value(runtime, object));
        }catch (JavetException e){
            throw new ScriptException(e);
        }
    }

    public void close() throws ScriptException {
        if(runtime != null){
            try {
                runtime.close();
            } catch (JavetException e) {
                throw new ScriptException(e);
            }
            runtime = null;
        }
    }

    public FastJavetClassConverter getConverter() {
        return converter;
    }
}
