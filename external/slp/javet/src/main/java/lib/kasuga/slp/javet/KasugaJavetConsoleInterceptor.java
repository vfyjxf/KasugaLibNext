package lib.kasuga.slp.javet;

import com.caoccao.javet.interception.logging.BaseJavetConsoleInterceptor;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.values.V8Value;
import com.mojang.logging.LogUtils;
import lib.kasuga.scripting.ScriptConsole;
import org.slf4j.Logger;

public class KasugaJavetConsoleInterceptor extends BaseJavetConsoleInterceptor {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ScriptConsole console;

    public KasugaJavetConsoleInterceptor(V8Runtime v8Runtime, ScriptConsole console) {
        super(v8Runtime);
        this.console = console;
    }

    @Override
    public void consoleDebug(V8Value... v8Values) {
        console.debug(concat(v8Values));
    }

    @Override
    public void consoleError(V8Value... v8Values) {
        console.error(concat(v8Values));
    }

    @Override
    public void consoleInfo(V8Value... v8Values) {
        console.info(concat(v8Values));
    }

    @Override
    public void consoleLog(V8Value... v8Values) {
        console.info(concat(v8Values));
    }

    @Override
    public void consoleTrace(V8Value... v8Values) {
        console.debug(concat(v8Values));
    }

    @Override
    public void consoleWarn(V8Value... v8Values) {
        console.warn(concat(v8Values));
    }
}
