package lib.kasuga.core.saved;

import com.mojang.datafixers.DataFixer;
import io.micronaut.context.annotation.Bean;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lib.kasuga.mixins.MinecraftServerAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.nio.file.Path;
import java.util.HashMap;

@Singleton()
public class CustomSavedDataManager {
    private MinecraftServer server;

    public CustomSavedDataManager(@Named("forgeEventBus") IEventBus eventBus) {
        eventBus.addListener(EventPriority.HIGHEST, this::onServerStarting);
        eventBus.addListener(EventPriority.LOWEST,this::onLevelSaving);
        eventBus.addListener(EventPriority.LOWEST,this::onServerStopping);
    }

    private HashMap<Class<?>, CustomSavedData> savedData = new HashMap<>();

    public void onServerStarting(ServerStartingEvent event) {
        this.server = event.getServer();
    }

    public void onLevelSaving(LevelEvent.Save event) {
        MinecraftServer server = event.getLevel().getServer();
        assert server != null;
        Path levelPath = getLevelPath(server);
        for (CustomSavedData savedDatum : this.savedData.values()) {
            savedDatum.save(levelPath, server.registryAccess());
        }
    }

    public void onServerStopping(ServerStoppingEvent event) {
        savedData.clear();
        this.server = null;
    }

    public Path getLevelPath(MinecraftServer server) {
        MinecraftServerAccess access = (MinecraftServerAccess) server;
        return access.getStorageSource().getLevelDirectory().path();
    }

    public <T extends CustomSavedData> T get(Class<T> classType, SimpleFactory<T> factory) {
        return get(classType, (path, provider, fixer) -> factory.create(path, provider));
    }

    public <T extends CustomSavedData> T get(Class<T> classType, Factory<T> factory) {
        if(this.savedData.containsKey(classType)){
            //noinspection unchecked
            return (T) this.savedData.get(classType);
        }
        Path levelPath = getLevelPath(this.server);
        T data = factory.create(levelPath, this.server.registryAccess(), this.server.getFixerUpper());
        if (data != null)
            this.savedData.put(classType, data);
        return data;
    }

    public interface Factory<T> {
        public T create(Path path, HolderLookup.Provider provider, DataFixer fixer);
    }

    public interface SimpleFactory<T> {
        public T create(Path path, HolderLookup.Provider provider);
    }
}
