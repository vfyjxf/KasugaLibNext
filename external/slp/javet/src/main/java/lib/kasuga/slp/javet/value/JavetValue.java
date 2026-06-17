package lib.kasuga.slp.javet.value;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.IV8Value;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueReference;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.value.ScriptValue;

public class JavetValue<T extends V8Value> implements ScriptValue {
    T delegate;

    public JavetValue(T delegate) {
        this.delegate = delegate;
    }

    @Override
    public String asString() throws ScriptException {
        try {
            return delegate.asString();
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public void close() throws ScriptException {
        try {
            if(delegate.isClosed())
                return;
            delegate.close();
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public JavetValue<T> cloneValue() throws ScriptException {
        return new JavetValue<>(cloneReference());
    }

    protected T cloneReference() throws ScriptException {
        try {
            T newDelegate = delegate.toClone();
            if(newDelegate instanceof V8ValueReference ref) {
                ref.setWeak();
            }
            return newDelegate;
        } catch (JavetException e) {
            throw new ScriptException(e);
        }
    }

    public T getDelegate() {
        return delegate;
    }
}
