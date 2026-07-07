package lib.kasuga.slp.javet.value;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.primitive.V8ValueNull;
import com.caoccao.javet.values.reference.V8ValueFunction;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.value.ScriptFunction;
import lib.kasuga.scripting.value.ScriptValue;

public class JavetValueFunction extends JavetValueObject<V8ValueFunction> implements ScriptFunction, IJavetBindReceiver {
    protected JavetValueObject<? extends V8Value> caller;

    public JavetValueFunction(V8ValueFunction delegate) {
        super(delegate);
    }

    @Override
    public ScriptValue execute(ScriptValue... arguments) throws ScriptException {
        try{
            return JavetValueBridge.wrap(delegate.call(caller == null ? this.delegate.getV8Runtime().createV8ValueNull() : caller.getDelegate(), (Object[])arguments));
        }catch (JavetException exception) {
            throw new ScriptException(exception);
        }
    }

    @Override
    public void executeVoid(ScriptValue... arguments) throws ScriptException {
        assertNotClosing();
        try{
            delegate.callVoid(caller == null ? this.delegate.getV8Runtime().createV8ValueNull() : caller.getDelegate(), (Object[]) arguments);
        }catch (JavetException exception) {
            throw new ScriptException(exception);
        }
    }

    @Override
    public void bind(JavetValueObject<? extends V8Value> caller) {
        if(this.caller != null) {
            this.caller.receivers.remove(this);
        }
        this.caller = caller;
        if(this.caller != null) {
            this.caller.receivers.add(this);
        }
    }

    @Override
    public void close() throws ScriptException {
        super.close();
        if(this.caller != null)
            this.caller.removeBindReceiver(this);
    }

    @Override
    public JavetValueFunction cloneValue() throws ScriptException {
        return new JavetValueFunction(cloneReference());
    }
}
