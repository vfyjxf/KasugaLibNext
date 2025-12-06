package lib.kasuga.slp.javet.value;

import com.caoccao.javet.values.V8Value;
import lib.kasuga.scripting.ScriptException;

public interface IJavetBindReceiver {
    public void bind(JavetValueObject<? extends V8Value> caller);

    void close() throws ScriptException;
}
