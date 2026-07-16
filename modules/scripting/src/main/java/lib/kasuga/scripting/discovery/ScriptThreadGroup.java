package lib.kasuga.scripting.discovery;

import jakarta.annotation.Nullable;
import lib.kasuga.scripting.ScriptEngine;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ScriptThreadGroup {
    private final String packageName;
    private final ThreadGroup threadGroup;
    private final Map<String, ScriptThread> threads = new HashMap<>();
    private final Set<ScriptThreadGroup> children = new HashSet<>();
    @Nullable
    private final ScriptThreadGroup parent;

    public ScriptThreadGroup(String packageName, @Nullable ScriptThreadGroup parent) {
        this.packageName = packageName;
        this.parent = parent;
        this.threadGroup = parent != null
            ? new ThreadGroup(parent.threadGroup, packageName)
            : new ThreadGroup(packageName);
    }

    public ScriptThread createThread(String entryName, ScriptEngine engine) {
        ScriptThread thread = new ScriptThread(this, engine, entryName);
        threads.put(entryName, thread);
        thread.start();
        return thread;
    }

    public void dispatchTick() {
        for (ScriptThread thread : threads.values()) {
            thread.dispatchTick();
        }
        for (ScriptThreadGroup child : children) {
            child.dispatchTick();
        }
    }

    public ScriptThreadGroup createChild(String childPackageName) {
        ScriptThreadGroup child = new ScriptThreadGroup(childPackageName, this);
        children.add(child);
        return child;
    }

    public CompletableFuture<Void> shutdown() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (ScriptThread thread : threads.values()) {
            futures.add(thread.shutdown());
        }
        for (ScriptThreadGroup child : children) {
            futures.add(child.shutdown());
        }
        threads.clear();
        children.clear();
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    void onThreadTerminated(ScriptThread thread) {
        // cleanup logic
    }

    public String getPackageName() { return packageName; }
    public ThreadGroup getThreadGroup() { return threadGroup; }
}
