package lib.kasuga.scripting;

import com.mojang.logging.LogUtils;
import jakarta.inject.Inject;
import lib.kasuga.core.resource.ScopedResourceManager;
import lib.kasuga.core.resource.ScopedResourcePackListener;
import lib.kasuga.core.resource.pack.ScopedPackResources;
import lib.kasuga.scripting.discovery.PackageLoadingError;
import lib.kasuga.scripting.discovery.PackageSystem;
import lib.kasuga.scripting.discovery.ScriptPackage;
import lib.kasuga.scripting.discovery.ScriptThreadGroup;
import lib.kasuga.scripting.module.EngineInstanceManager;
import lib.kasuga.scripting.module.PackageRegistry;
import lib.kasuga.scripting.module.ResolvedPackage;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ScriptRuntime implements ScopedResourcePackListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ScopedResourceManager resourceManager;
    private ScriptThreadGroup rootThreadGroup;
    private ScriptPackage rootPackage;

    @Inject()
    PackageSystem packageSystem;

    @Inject()
    PackageRegistry packageRegistry;

    @Inject()
    EngineInstanceManager instanceManager;

    public ScriptRuntime(@Nullable MinecraftServer server, ScopedResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void close() {
        if (rootThreadGroup != null) {
            rootThreadGroup.shutdown();
        }
    }

    @Override
    public void onReloaded(ScopedResourceManager resourceManager) {
        if (rootThreadGroup != null) {
            rootThreadGroup.shutdown().join();
        }

        packageSystem.init();
        packageRegistry.clear();
        instanceManager.clear();

        rootThreadGroup = new ScriptThreadGroup("ScriptRuntime", null);
        rootPackage = new ScriptPackage();

        for (ScopedPackResources packResource : resourceManager.getPackResources()) {
            try {
                List<ResolvedPackage> discovered = packageSystem.scan(packResource);
                for (ResolvedPackage pkg : discovered) {
                    ScriptPackage scriptPkg = new ScriptPackage(pkg);
                    attachToParent(rootPackage, scriptPkg);
                }
            } catch (PackageLoadingError error) {
                packageSystem.addLoadingError(error);
                LOGGER.error("Failed to load script package from resource pack: " + packResource.getName(), error);
            }
        }

        rootPackage.start(rootThreadGroup, instanceManager);
    }

    private void attachToParent(ScriptPackage root, ScriptPackage candidate) {
        if (candidate.resolved() == null) {
            root.addChild(candidate);
            return;
        }

        String candidateRoot = candidate.resolved().packRelativeRoot();
        for (ScriptPackage child : root.children()) {
            if (child.resolved() == null) continue;
            String childRoot = child.resolved().packRelativeRoot();
            if (candidateRoot.startsWith(childRoot + "/")) {
                attachToParent(child, candidate);
                return;
            }
        }

        root.addChild(candidate);
    }

    public void tick() {
        if (rootThreadGroup != null) {
            rootThreadGroup.dispatchTick();
        }
    }
}
