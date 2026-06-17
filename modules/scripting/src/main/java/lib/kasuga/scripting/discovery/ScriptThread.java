package lib.kasuga.scripting.discovery;

import com.mojang.logging.LogUtils;
import lib.kasuga.scripting.ScriptEngine;
import lib.kasuga.scripting.ScriptException;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScriptThread extends Thread {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ScriptThreadGroup group;
    private final ScriptEngine engine;
    private final Queue<Runnable> pendingTasks = new ArrayDeque<>();
    private final AtomicBoolean shouldShutdown = new AtomicBoolean(false);
    private final AtomicBoolean tickReady = new AtomicBoolean(false);
    private final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();

    ScriptThread(ScriptThreadGroup group, ScriptEngine engine, String entryName) {
        super(group.getThreadGroup(), "Script-" + group.getPackageName() + "-" + entryName);
        this.group = group;
        this.engine = engine;
    }

    @Override
    public void run() {
        try {
            while (!shouldShutdown.get()) {
                try {
                    Runnable task;
                    synchronized (pendingTasks) {
                        task = pendingTasks.poll();
                    }
                    if (task != null) task.run();

                    engine.tick();

                    synchronized (this) {
                        while (!tickReady.get() && !shouldShutdown.get()) {
                            this.wait();
                        }
                        tickReady.set(false);
                    }
                } catch (InterruptedException e) {
                    if (shouldShutdown.get()) break;
                } catch (RuntimeException e) {
                    LOGGER.error("Error in script thread {}", getName(), e);
                }
            }
        } finally {
            engine.close();
            group.onThreadTerminated(this);
            shutdownFuture.complete(null);
        }
    }

    void dispatchTick() {
        tickReady.set(true);
        synchronized (this) { this.notifyAll(); }
    }

    public void recordCall(Runnable task) {
        synchronized (pendingTasks) { pendingTasks.add(task); }
    }

    public CompletableFuture<Void> shutdown() {
        shouldShutdown.set(true);
        this.interrupt();
        return shutdownFuture;
    }
}
