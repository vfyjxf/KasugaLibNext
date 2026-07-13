package lib.kasuga.scripting.value;

import lib.kasuga.scripting.ScriptException;

public interface ScriptReference {
    public void pin() throws ScriptException;
    public void removePin() throws ScriptException;

    public static void pinFor(ScriptValue maybeReference) throws ScriptException {
        if(maybeReference instanceof ScriptReference reference) {
            reference.pin();
        }
    }

    public static void removePinFor(ScriptValue maybeReference) throws ScriptException {
        if(maybeReference instanceof ScriptReference reference) {
            reference.removePin();
        }
    }
}
