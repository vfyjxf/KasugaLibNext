package lib.kasuga.scripting.discovery;

import com.mojang.logging.LogUtils;
import jakarta.annotation.Nullable;
import lib.kasuga.scripting.ScriptConsole;
import lib.kasuga.scripting.ScriptEngine;
import lib.kasuga.scripting.ScriptEngineType;
import lib.kasuga.scripting.ScriptException;
import lib.kasuga.scripting.module.ResolvedPackage;
import lib.kasuga.scripting.module.ResolvedScript;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ScriptPackage {

    private static final Logger LOGGER = LogUtils.getLogger();

    @lombok.Getter
    private final ResolvedPackage resolved;

    private final List<ScriptPackage> children = new ArrayList<>();

    @lombok.Getter
    private ScriptThreadGroup threadGroup;

    public ScriptPackage(ResolvedPackage resolved) {
        this.resolved = resolved;
    }

    public ScriptPackage() {
        this.resolved = null;
    }

    public boolean isRunnable() {
        return resolved != null;
    }

    @Nullable
    public PackageInfo info() {
        return resolved == null ? null : resolved.info();
    }

    @Nullable
    public ScriptEngineType<?> engine() {
        return resolved == null ? null : resolved.engine();
    }

    public void addChild(ScriptPackage child) {
        children.add(child);
    }

    public List<ScriptPackage> children() {
        return children;
    }

    public void start(ScriptThreadGroup parentGroup) {
        if (!isRunnable()) {
            for (ScriptPackage child : children) {
                if (!child.isRunnable()) continue;
                child.start(parentGroup);
            }
            return;
        }
        String groupName = resolved.info().name() != null
                ? resolved.info().name()
                : "anonymous-" + resolved.packRelativeRoot();
        LOGGER.info("[ScriptPackage] Starting package '{}'", groupName);
        this.threadGroup = parentGroup.createChild(groupName);

        ScriptConsole console = new ScriptConsole() {
            @Override public void log(String message) { LOGGER.info("[{}] {}", groupName, message); }
            @Override public void warn(String message) { LOGGER.warn("[{}] {}", groupName, message); }
            @Override public void debug(String message) { LOGGER.debug("[{}] {}", groupName, message); }
            @Override public void info(String message) { LOGGER.info("[{}] {}", groupName, message); }
            @Override public void error(String message) { LOGGER.error("[{}] {}", groupName, message); }
        };

        ScriptEngine engineInstance;
        try {
            engineInstance = resolved.engine().create(console);
        } catch (ScriptException e) {
            LOGGER.error("Failed to create engine for package '{}'", groupName, e);
            return;
        }
        ScriptThread thread = threadGroup.createThread(groupName, engineInstance);

        Stream.of(
                resolved.info().entry().common(),
                resolved.info().entry().server(),
                resolved.info().entry().client()
        ).flatMap(List::stream).forEach(entry -> {
                LOGGER.info("[ScriptPackage] Scheduling entry '{}' for package '{}'", entry, groupName);
                thread.recordCall(() -> executeEntry(engineInstance, entry));
        });

        for (ScriptPackage child : children) {
            if (!child.isRunnable()) continue;
            child.start(threadGroup);
        }
    }

    private void executeEntry(ScriptEngine engine, String entryName) {
        try {
            LOGGER.info("[ScriptPackage] Executing entry '{}' in package '{}'", entryName, resolved.info().name());
            ResolvedScript script = resolved.engine().resolver.locateScript(resolved, List.of(entryName));
            if(script == null) {
                LOGGER.error("[ScriptPackage] Entry script '{}' not found in package '{}'", entryName, resolved.info().name());
                return;
            }
            try (InputStream is = script.open()) {
                engine.executeEntry(entryName, is);
                LOGGER.info("[ScriptPackage] Entry '{}' executed successfully in package '{}'", entryName, resolved.info().name());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to execute entry '{}' in package '{}'", entryName, resolved.info().name(), e);
        }
    }
}
