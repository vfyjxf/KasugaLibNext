package lib.kasuga.slp.javet;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueFunction;
import com.caoccao.javet.values.reference.V8ValueObject;
import lib.kasuga.scripting.ScriptConsole;
import lib.kasuga.scripting.ScriptEngine;
import lib.kasuga.scripting.ScriptEngineType;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.module.ResolvedScript;
import lib.kasuga.scripting.module.ScriptModuleHandle;
import lib.kasuga.scripting.value.ScriptValue;
import lib.kasuga.slp.javet.converter.FastJavetClassConverter;
import lib.kasuga.slp.javet.module.JavetModuleHandle;
import lib.kasuga.slp.javet.module.RequireResolver;
import lib.kasuga.slp.javet.value.JavetValueBridge;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class JavetScriptEngine implements ScriptEngine {
    @Getter V8Runtime runtime;
    private FastJavetClassConverter converter;
    private ScriptEngineType<?> type;
    @Setter
    private RequireResolver requireResolver;
    private final Map<String, ScriptModuleHandle> loadedModules = new HashMap<>();
    private int gcTicks = 0;

    public JavetScriptEngine() {}

    public void setType(ScriptEngineType<?> type) {
        this.type = type;
    }

    @Override
    public ScriptEngineType<?> getType() {
        return type;
    }

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
                    console.error("Unhandled Promise Rejection: " + com.caoccao.javet.utils.V8ValueUtils.asString(value));
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

    @Override
    public ScriptModuleHandle loadModule(ResolvedScript script) throws ScriptException {
        String sourcePath = script.filePath();

        ScriptModuleHandle cached = loadedModules.get(sourcePath);
        if (cached != null) return cached;

        // Register partial handle to support circular requires
        // (not yet populated, but present in cache to break cycles)
        loadedModules.put(sourcePath, null);

        String source = readSource(script);
        try {
            V8ValueFunction moduleFunc = runtime.getExecutor(source)
                .setResourceName(sourcePath)
                .compileV8ValueFunction(new String[]{"require", "exports", "module"});

            V8ValueObject moduleObj = runtime.createV8ValueObject();
            V8ValueObject exportsObj = runtime.createV8ValueObject();
            moduleObj.set("exports", exportsObj);

            String fromSourcePath = sourcePath;
            V8Value requireFunc = createRequireFunc(fromSourcePath);

            try {
                moduleFunc.callVoid(null, requireFunc, exportsObj, moduleObj);

                V8Value resultExports = moduleObj.get("exports");

                Map<String, ScriptValue> exports = new HashMap<>();
                if (resultExports instanceof V8ValueObject resultObj) {
                    var propNames = resultObj.getOwnPropertyNames();
                    int len = propNames.getLength();
                    for (int i = 0; i < len; i++) {
                        String key = propNames.getString(i);
                        V8Value val = resultObj.get(key);
                        exports.put(key, JavetValueBridge.wrap(val));
                    }
                }

                JavetModuleHandle handle = new JavetModuleHandle(exports, sourcePath, type);
                loadedModules.put(sourcePath, handle);
                return handle;
            } finally {
                requireFunc.close();
                moduleObj.close();
            }
        } catch (JavetException e) {
            loadedModules.remove(sourcePath);
            throw new ScriptException(e);
        }
    }

    @Override
    public ScriptModuleHandle getLoadedModule(String sourcePath) {
        return loadedModules.get(sourcePath);
    }

    @Override
    public void executeEntry(String entryName, InputStream source) throws ScriptException {
        String code = readSource(source);
        try {
            V8ValueFunction moduleFunc = runtime.getExecutor(code)
                .setResourceName(entryName)
                .compileV8ValueFunction(new String[]{"require", "exports", "module"});

            V8ValueObject moduleObj = runtime.createV8ValueObject();
            V8ValueObject exportsObj = runtime.createV8ValueObject();
            moduleObj.set("exports", exportsObj);

            V8Value requireFunc = createRequireFunc(entryName);

            try {
                moduleFunc.callVoid(null, requireFunc, exportsObj, moduleObj);
            } finally {
                requireFunc.close();
                moduleObj.close();
            }
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public void tick() {
        if (gcTicks++ > 20) {
            runtime.lowMemoryNotification();
            gcTicks = 0;
        }
    }

    public void close() {
        loadedModules.clear();

        if(runtime != null){
            try {
                runtime.close();
            } catch (JavetException e) {
                // ignore
            }
            runtime = null;
        }
    }

    public FastJavetClassConverter getConverter() {
        return converter;
    }

    private V8Value createRequireFunc(String fromSourcePath) throws JavetException {
        return runtime.getConverter().toV8Value(runtime,
            (java.util.function.Function<String, V8Value>) moduleName -> {
                if (requireResolver == null) {
                    throw new RuntimeException("No require resolver set");
                }
                try {
                    ScriptModuleHandle handle = requireResolver.resolve(moduleName, fromSourcePath);
                    if (handle == null) {
                        throw new RuntimeException("Module not found: " + moduleName);
                    }
                    // Convert handle exports to a V8 object
                    try {
                        V8ValueObject exports = runtime.createV8ValueObject();
                        for (String key : handle.getExportNames()) {
                            ScriptValue val = handle.getExport(key);
                            if (val != null) {
                                exports.set(key, converter.toV8Value(runtime, val));
                            }
                        }
                        return exports;
                    } catch (JavetException e) {
                        throw new RuntimeException(e);
                    }
                } catch (ScriptException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    private String readSource(ResolvedScript script) throws ScriptException {
        try (InputStream is = script.open()) {
            return readSource(is);
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    private String readSource(InputStream is) throws ScriptException {
        try (var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }
}
