package lib.kasuga.scripting;

import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import lib.kasuga.core.resource.ResourceSystem;
import lib.kasuga.core.resource.ScopedResourceManager;
import lib.kasuga.core.resource.ScopedResourceManagerConsumer;
import lib.kasuga.core.resource.ServerResourceReloadFinishEvent;
import lib.kasuga.scripting.discovery.PackageSystem;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@Context()
public class ScriptingApplication {
    @Inject()
    ResourceSystem system;
    @Inject()
    ScriptingSystem scriptingSystem;

    @PostConstruct
    public void init() {
        system.registerConsumer(scriptingSystem);
    }
}
