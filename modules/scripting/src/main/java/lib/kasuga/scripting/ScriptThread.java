package lib.kasuga.scripting;

public class ScriptThread extends Thread {
    public ScriptThread(ThreadGroup threadGroup, String packageName) {
        super(threadGroup, "Javascript Thread " + packageName);
    }
}
