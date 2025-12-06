package lib.kasuga.scripting;

public class ScriptException extends Exception {
    public ScriptException(Exception e) {
        super(e);
    }

    public ScriptException(String s) {
        super(s);
    }
}
