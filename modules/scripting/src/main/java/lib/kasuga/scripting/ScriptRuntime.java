package lib.kasuga.scripting;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.mojang.logging.LogUtils;
import lib.kasuga.core.resource.ScopedResourceManager;
import lib.kasuga.core.resource.ScopedResourcePackListener;
import lib.kasuga.core.resource.pack.FlattenScopedPackResources;
import lib.kasuga.core.resource.pack.HierarchicalScopedPackResources;
import lib.kasuga.core.resource.pack.ScopedPackResources;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;

public class ScriptRuntime implements ScopedResourcePackListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ScopedResourceManager resourceManager;
    private final ThreadGroup threadGroup;

    public ScriptRuntime(@Nullable MinecraftServer server, ScopedResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        this.threadGroup = new ThreadGroup("ScriptRuntime" + (server != null ? "Server - " + server.hashCode() : "Client"));
    }

    public void close() {}

    @Override
    public void onReloaded(ScopedResourceManager resourceManager) {
        threadGroup.interrupt();
        // while(threadGroup.activeCount() > 0) {}
        TomlParser tomlParser = new TomlParser();
        for (ScopedPackResources packResource : resourceManager.getPackResources()) {
            try{
                if(packResource instanceof HierarchicalScopedPackResources hierarchical) {
                    System.out.println(hierarchical.list("", ""));
                } else if(packResource instanceof FlattenScopedPackResources flatten) {
                    System.out.println(flatten.listEntries(""));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void tick() {

    }
}
