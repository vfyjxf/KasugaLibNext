package lib.kasuga.slp.javet.value;


import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.IV8Value;
import com.caoccao.javet.values.reference.*;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.value.ScriptObject;
import lib.kasuga.scripting.value.ScriptValue;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class JavetValueObject<T extends V8ValueObject> extends JavetValue<T> implements ScriptObject {
    protected Set<IJavetBindReceiver> receivers = Collections.newSetFromMap(new WeakHashMap<>());
    private boolean closing = false;
    private boolean closed = false;

    public JavetValueObject(T delegate) {
        super(delegate);
    }

    @Override
    public ScriptValue getMember(ScriptValue key) throws ScriptException {
        assertNotClosing();
        IV8Value convertedKey = null;
        try{
            convertedKey = this.delegate.getV8Runtime().getConverter().toV8Value(this.delegate.getV8Runtime(), key);
            IV8Value value = delegate.get(convertedKey);
            if(value instanceof IV8ValueReference reference) {
                reference.clearWeak();
            }
            ScriptValue scriptValue = JavetValueBridge.wrap(value);
            if(scriptValue instanceof IJavetBindReceiver receiver) {
                receiver.bind(this);
                addBindReceiver(receiver);
            }
            return scriptValue;
        } catch (JavetException e) {
            throw new ScriptException(e);
        } finally {
            JavetResourceUtils.safeClose(convertedKey);
        }
    }

    @Override
    public ScriptValue setMember(ScriptValue key, ScriptValue member) throws ScriptException {
        assertNotClosing();
        IV8Value convertedKey = null;
        IV8Value convertedMember = null;
        try{
            convertedKey = this.delegate.getV8Runtime().getConverter().toV8Value(this.delegate.getV8Runtime(), key);
            convertedMember = this.delegate.getV8Runtime().getConverter().toV8Value(this.delegate.getV8Runtime(), member);
            delegate.set(convertedKey, convertedMember);
            return member;
        } catch (JavetException e) {
            throw new ScriptException(e);
        } finally {
            JavetResourceUtils.safeClose(convertedKey, convertedMember);
        }
    }

    @Override
    public ScriptValue[] getObjectKeys() throws ScriptException {
        assertNotClosing();
        IV8ValueArray keys = null;
        IV8Value[] rollbackArray = null;
        try {
            keys = delegate.getOwnPropertyNames();
            int length = keys.getLength();

            ScriptValue[] result = new ScriptValue[length];
            rollbackArray = new IV8Value[length];
            for (int i = 0; i < length; i++) {
                IV8Value value = keys.get(i);
                rollbackArray[i] = value;
                if(value instanceof IV8ValueReference reference) {
                    reference.clearWeak();
                }
                result[i] = JavetValueBridge.wrap(value);
            }
            return result;
        } catch (JavetException e) {
            JavetResourceUtils.safeClose(keys, rollbackArray);
            throw new ScriptException(e);
        }
    }

    @Override
    public void remove(ScriptValue key) throws ScriptException {
        assertNotClosing();
        IV8Value convertedKey = null;
        try{
            convertedKey = this.delegate.getV8Runtime().getConverter().toV8Value(this.delegate.getV8Runtime(), key);
            delegate.delete(convertedKey);
        } catch (JavetException e) {
            throw new ScriptException(e);
        } finally {
            JavetResourceUtils.safeClose(convertedKey);
        }
    }

    @Override
    public void close() throws ScriptException {
        close(false);
    }

    public void close(boolean forceAndCascade) throws ScriptException {
        this.closing = true;

        if(!receivers.isEmpty() && !forceAndCascade)
            return;

        for (IJavetBindReceiver receiver : this.receivers) {
            receiver.close();
        }

        this.receivers.clear();

        this.closed = true;

        super.close();
    }

    protected void assertNotClosing() throws ScriptException {
        if(this.closing) {
            throw new ScriptException("Cannot access a V8 value in CLOSING state");
        }
        if(delegate.isClosed() || this.closed) {
            throw new ScriptException("Cannot access a CLOSED V8 value");
        }
    }

    protected void addBindReceiver(IJavetBindReceiver receiver) throws ScriptException {
        assertNotClosing();
        this.receivers.add(receiver);
    }

    protected void removeBindReceiver(IJavetBindReceiver receiver) throws ScriptException {
        this.receivers.remove(receiver);
        if(closing && !closed){
            closed = true;
            if(!this.delegate.isClosed())
                super.close();
        }
    }
}
