package lib.kasuga.scripting;

public class ScriptThread extends Thread {
    public ScriptThread(ThreadGroup threadGroup, String languageName, String packageName) {
        super(threadGroup, languageName + " Thread " + packageName);
    }
}
