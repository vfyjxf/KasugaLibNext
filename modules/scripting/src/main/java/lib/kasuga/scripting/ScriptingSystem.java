package lib.kasuga.scripting;

import io.micronaut.context.annotation.Context;
import jakarta.inject.Inject;
import lib.kasuga.KasugaLib;
import lib.kasuga.core.resource.ScopedResourceManager;
import lib.kasuga.core.resource.ScopedResourceManagerConsumer;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

@Context()
public class ScriptingSystem implements ScopedResourceManagerConsumer {

    HashMap<MinecraftServer, ScriptRuntime> serverRuntime = new HashMap<>();
    HashMap<ScopedResourceManager, ScriptRuntime> runtime = new HashMap<>();

    @Override
    public void onResourceManagerAdded(@Nullable MinecraftServer server, ScopedResourceManager resourceManager) {
        ScriptRuntime rt = new ScriptRuntime(server, resourceManager);
        KasugaLib.getContext().inject(rt);
        runtime.put(resourceManager, rt);
        resourceManager.addListener(rt);
        serverRuntime.put(server, rt);
    }

    @Override
    public void onResourceManagerRemoved(@Nullable MinecraftServer server, ScopedResourceManager resourceManager) {
        ScriptRuntime rt = runtime.remove(resourceManager);
        serverRuntime.remove(server);
        if (rt != null) {
            rt.close();
        }
    }

    public void onServerTick(ServerTickEvent.Pre event) {
        if(serverRuntime.containsKey(event.getServer()))
            serverRuntime.get(event.getServer()).tick();
    }

    public void onClientTick(ClientTickEvent.Pre event) {
        if(serverRuntime.containsKey(null))
            serverRuntime.get(null).tick();
    }
}
