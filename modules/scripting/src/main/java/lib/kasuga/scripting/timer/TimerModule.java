package lib.kasuga.scripting.timer;

import com.mojang.logging.LogUtils;
import jakarta.annotation.Nullable;
import lib.kasuga.scripting.ScriptEngine;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.security.Api;
import lib.kasuga.scripting.module.ScriptModule;
import lib.kasuga.scripting.module.ScriptModuleFactory;
import lib.kasuga.scripting.value.ScriptFunction;
import lib.kasuga.scripting.value.ScriptReference;
import lib.kasuga.scripting.value.ScriptValue;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class TimerModule implements ScriptModule {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ScriptModuleFactory<TimerModule> FACTORY = new ScriptModuleFactory<>() {
        @Override
        public String name() { return "kasuga:timer"; }

        @Override
        public TimerModule create(ScriptEngine engine) { return new TimerModule(); }
    };

    private final KasugaTimer timer = new KasugaTimer();
    private final Map<Integer, Runnable> cleanupActions = new HashMap<>();
    private volatile boolean closed = false;

    @Override
    public void init() throws ScriptException {}

    @Api
    public int setInterval(ScriptValue callback, int ms) throws ScriptException {
        ScriptFunction func = extractFunction(callback);
        if(func == null) return -1;
        ScriptFunction clone = (ScriptFunction) func.cloneValue();
        if(clone instanceof ScriptReference ref) ref.pin();
        int ticks = Math.max(1, ms / 50);
        int id = timer.register(KasugaTimer.TimerType.INTERVAL, () -> {
            if(closed) return;
            try {
                clone.executeVoid();
            } catch (Exception e) {
                LOGGER.warn("setInterval callback error: ", e);
            }
        }, ticks);
        cleanupActions.put(id, () -> unpinAndClose(clone));
        return id;
    }

    @Api
    public int setTimeout(ScriptValue callback, int ms) throws ScriptException {
        ScriptFunction func = extractFunction(callback);
        if(func == null) return -1;
        ScriptFunction clone = (ScriptFunction) func.cloneValue();
        if(clone instanceof ScriptReference ref) ref.pin();
        int ticks = Math.max(1, ms / 50);
        int id = timer.register(KasugaTimer.TimerType.TIMEOUT, () -> {
            if(closed) return;
            try {
                clone.executeVoid();
            } catch (Exception e) {
                LOGGER.warn("setTimeout callback error: ", e);
            }
        }, ticks);
        cleanupActions.put(id, () -> unpinAndClose(clone));
        return id;
    }

    @Api
    public void clearInterval(@Nullable Integer id) throws ScriptException {
        if(id == null) return;
        timer.unregister(id);
        Runnable cleanup = cleanupActions.remove(id);
        if(cleanup != null) cleanup.run();
    }

    @Api
    public void clearTimeout(@Nullable Integer id) throws ScriptException {
        clearInterval(id);
    }

    @Override
    public void tick() {
        if(closed) return;
        timer.onTick();
    }

    @Override
    public void close() {
        closed = true;
        for(Runnable cleanup : cleanupActions.values()) {
            try { cleanup.run(); } catch (Exception ignored) {}
        }
        cleanupActions.clear();
        timer.close();
    }

    @Nullable
    private ScriptFunction extractFunction(ScriptValue value) {
        if(value instanceof ScriptFunction func) {
            return func;
        }
        return null;
    }

    private void unpinAndClose(ScriptValue value) {
        try {
            if(value instanceof ScriptReference ref) ref.removePin();
        } catch (Exception ignored) {}
        try {
            value.close();
        } catch (Exception ignored) {}
    }
}
