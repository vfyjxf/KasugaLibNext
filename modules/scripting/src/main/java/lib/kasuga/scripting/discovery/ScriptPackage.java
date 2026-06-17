package lib.kasuga.scripting.discovery;

import com.mojang.logging.LogUtils;
import jakarta.annotation.Nullable;
import lib.kasuga.scripting.ScriptEngine;
import lib.kasuga.scripting.ScriptEngineType;
import lib.kasuga.scripting.module.EngineInstanceManager;
import lib.kasuga.scripting.module.ResolvedPackage;
import lib.kasuga.scripting.module.ResolvedScript;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ScriptPackage {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    private final ResolvedPackage resolved;

    private final List<ScriptPackage> children = new ArrayList<>();
    @Nullable
    private ScriptThreadGroup threadGroup;

    public ScriptPackage(ResolvedPackage resolved) {
        this.resolved = resolved;
    }

    public ScriptPackage() {
        this.resolved = null;
    }

    @Nullable
    public PackageInfo info() { return resolved != null ? resolved.info() : null; }

    @Nullable
    public ScriptEngineType<?> engine() { return resolved != null ? resolved.engine() : null; }

    @Nullable
    public ResolvedPackage resolved() { return resolved; }

    public List<ScriptPackage> children() { return children; }

    public void addChild(ScriptPackage child) { children.add(child); }

    public void start(ScriptThreadGroup parentGroup, EngineInstanceManager instanceManager) {
        if (resolved == null) return;

        PackageInfo info = resolved.info();
        ScriptEngineType<?> engineType = resolved.engine();

        String groupName = info.name() != null ? info.name() : "anonymous-" + resolved.packRelativeRoot();
        threadGroup = parentGroup.createChild(groupName);

        ScriptEngine engineInstance = instanceManager.getOrCreate(engineType, null);

        for (String entry : info.entry().common()) {
            ScriptThread thread = threadGroup.createThread(entry, engineInstance);
            thread.recordCall(() -> executeEntry(engineInstance, entry));
        }

        for (String entry : info.entry().server()) {
            ScriptThread thread = threadGroup.createThread("server:" + entry, engineInstance);
            thread.recordCall(() -> executeEntry(engineInstance, entry));
        }

        for (String entry : info.entry().client()) {
            ScriptThread thread = threadGroup.createThread("client:" + entry, engineInstance);
            thread.recordCall(() -> executeEntry(engineInstance, entry));
        }

        for (ScriptPackage child : children) {
            child.start(threadGroup, instanceManager);
        }
    }

    private void executeEntry(ScriptEngine engine, String entryName) {
        ResolvedScript script = resolved.engine().resolver.locateScript(
            resolved, List.of(entryName)
        );
        if (script == null) {
            LOGGER.error("Entry script not found: {} in package {}", entryName, resolved.info().name());
            return;
        }
        try (InputStream source = script.open()) {
            engine.executeEntry(entryName, source);
        } catch (Exception e) {
            LOGGER.error("Failed to execute entry script: {}", entryName, e);
        }
    }

    public CompletableFuture<Void> shutdown() {
        if (threadGroup != null) {
            return threadGroup.shutdown();
        }
        return CompletableFuture.completedFuture(null);
    }

    public void dispatchTick() {
        if (threadGroup != null) {
            threadGroup.dispatchTick();
        }
    }

    public void assemble() {
        // no-op, kept for compatibility
    }
}
