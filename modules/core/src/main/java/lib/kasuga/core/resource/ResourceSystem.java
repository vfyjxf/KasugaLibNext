package lib.kasuga.core.resource;

import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Context()
public class ResourceSystem {
    ScopedResourceManager client = null;
    HashMap<MinecraftServer, ScopedResourceManager> managers = new HashMap<>();

    public void onServerReloading(MinecraftServer server) {
        ScopedResourceManager scopedResourceManager = managers.get(server);
        if(scopedResourceManager != null) {
            scopedResourceManager.reload(server.getResourceManager());
        }
    }

    @Inject() @Named("forgeEventBus")
    IEventBus eventBus;

    @Inject @Named("modEventBus")
    IEventBus modEventBus;

    @PostConstruct()
    public void init() {
        eventBus.addListener(this::onServerStarting);
        eventBus.addListener(this::onServerStopping);
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceManager manager = minecraft.getResourceManager();
        ScopedResourceManager srm = new ScopedResourceManager(manager);
        client = srm;
        if(manager instanceof ReloadableResourceManager reloadable) {
            reloadable.registerReloadListenerIfNotPresent(srm);
        }
        for (ScopedResourceManagerConsumer consumer : this.consumers) {
            consumer.onResourceManagerAdded(null, client);
        }
    }

    private void onServerStarting(ServerStartingEvent event) {
        managers.computeIfAbsent(
                event.getServer(),
                rm -> {
                    ScopedResourceManager srm = new ScopedResourceManager(rm.getResourceManager());
                    if(rm.getResourceManager() instanceof ReloadableResourceManager reloadable) {
                        reloadable.registerReloadListenerIfNotPresent(srm);
                    }
                    return srm;
                }
        );
        for (ScopedResourceManagerConsumer consumer : this.consumers) {
            consumer.onResourceManagerAdded(event.getServer(), managers.get(event.getServer()));
        }
    }

    private void onServerStopping(ServerStoppingEvent event) {
        managers.remove(event.getServer());
        for (ScopedResourceManagerConsumer consumer : this.consumers) {
            consumer.onResourceManagerRemoved(event.getServer(), managers.get(event.getServer()));
        }
    }

    public ScopedResourceManager getServerManager(MinecraftServer server) {
        if(server == null) return client;
        return managers.get(server);
    }

    public ScopedResourceManager getManager(Level level) {
        if(level.isClientSide) {
            return getServerManager(null);
        } else if(level instanceof ServerLevel serverLevel) {
            return getServerManager(serverLevel.getServer());
        }
        throw new IllegalStateException("Unknown level: " + level);
    }

    public List<ScopedResourceManagerConsumer> consumers = new ArrayList<>();

    public void registerConsumer(ScopedResourceManagerConsumer consumer) {
        consumers.add(consumer);
        if(this.client != null)
            consumer.onResourceManagerAdded(null, client);
        for (var entry : managers.entrySet()) {
            consumer.onResourceManagerAdded(entry.getKey(), entry.getValue());
        }
    }
}
