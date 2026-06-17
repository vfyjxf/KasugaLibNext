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
import lib.kasuga.scripting.module.PackageRegistry;
import lib.kasuga.scripting.module.ResolvedPackage;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;

public class ScriptRuntime implements ScopedResourcePackListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ScopedResourceManager resourceManager;
    private ScriptThreadGroup rootThreadGroup;
    private ScriptPackage rootPackage;

    @Inject()
    PackageSystem packageSystem;

    @Inject()
    PackageRegistry packageRegistry;

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

        rootThreadGroup = new ScriptThreadGroup("ScriptRuntime", null);
        rootPackage = new ScriptPackage();

        for (ScopedPackResources packResource : resourceManager.getPackResources()) {
            try {
                List<ResolvedPackage> discovered = packageSystem.scan(packResource);
                LOGGER.info("[ScriptRuntime] Scanned pack '{}': found {} packages", packResource.getName(), discovered.size());
                for (ResolvedPackage pkg : discovered) {
                    LOGGER.info("[ScriptRuntime] Discovered package '{}' (engine={})", pkg.info().name(), pkg.info().engine());
                    ScriptPackage scriptPkg = new ScriptPackage(pkg);
                    attachToParent(rootPackage, scriptPkg);
                }
            } catch (PackageLoadingError error) {
                packageSystem.addLoadingError(error);
                LOGGER.error("Failed to load script package from resource pack: " + packResource.getName(), error);
            }
        }

        rootPackage.start(rootThreadGroup);
    }

    private void attachToParent(ScriptPackage root, ScriptPackage candidate) {
        if (candidate.getResolved() == null) {
            root.addChild(candidate);
            return;
        }

        String candidateRoot = candidate.getResolved().packRelativeRoot();
        for (ScriptPackage child : root.children()) {
            if (child.getResolved() == null) continue;
            String childRoot = child.getResolved().packRelativeRoot();
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
