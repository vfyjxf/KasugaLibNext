package lib.kasuga.scripting;

import com.mojang.logging.LogUtils;
import jakarta.inject.Inject;
import lib.kasuga.core.resource.ScopedResourceManager;
import lib.kasuga.core.resource.ScopedResourcePackListener;
import lib.kasuga.core.resource.pack.ScopedPackResources;
import lib.kasuga.scripting.discovery.PackageLoadingError;
import lib.kasuga.scripting.discovery.PackageSystem;
import lib.kasuga.scripting.discovery.ScriptMetadata;
import lib.kasuga.scripting.discovery.ScriptPackage;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ScriptRuntime implements ScopedResourcePackListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ScopedResourceManager resourceManager;
    private final ThreadGroup threadGroup;

    @Inject()
    PackageSystem packageSystem;

    public ScriptRuntime(@Nullable MinecraftServer server, ScopedResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        this.threadGroup = new ThreadGroup("ScriptRuntime" + (server != null ? "Server - " + server.hashCode() : "Client"));
    }

    public void close() {}

    @Override
    public void onReloaded(ScopedResourceManager resourceManager) {
        threadGroup.interrupt();
        
//        while(threadGroup.activeCount() > 0) {
//            threadGroup.interrupt();
//        }
        
        packageSystem.init();

        ScriptPackage rootPackage = new ScriptPackage();

        for (ScopedPackResources packResource : resourceManager.getPackResources()) {

            ScriptPackage packagePackRoot = new ScriptPackage();

            if(packageSystem.hasLoadingErrors())
                continue;

            try{
                ScriptMetadata metadata = packageSystem.readMetadata(packResource);

                if(metadata == null)
                    continue;

                List<ScriptEngineType<?>> engines = packageSystem.ensureEngineRequirement(metadata);

                List<ScriptPackage> enginePackages = packageSystem.resolve(packResource, engines);

                enginePackages.forEach(packagePackRoot::addChild);

                rootPackage.addChild(packagePackRoot);
            }catch (PackageLoadingError error) {
                packageSystem.addLoadingError(error);
                LOGGER.error("Failed to load script package from resource pack: " + packResource.getName(), error);
                throw error;
            }

            rootPackage.assemble();

            // rootPackage.start(threadGroup);
        }
    }

    public void tick() {

    }
}
