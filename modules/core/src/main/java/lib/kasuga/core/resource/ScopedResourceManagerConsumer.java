package lib.kasuga.core.resource;

import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface ScopedResourceManagerConsumer {
    public void onResourceManagerAdded(@Nullable MinecraftServer server, ScopedResourceManager resourceManager);
    public void onResourceManagerRemoved(@Nullable MinecraftServer server, ScopedResourceManager resourceManager);
}
