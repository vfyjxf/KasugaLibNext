package lib.kasuga.core.saved;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.mojang.datafixers.DataFixer;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScopedNbtSavedData<T extends SavedData> extends CustomSavedData {
    private final Path path;
    private final SavedData.Factory<T> factory;

    DimensionDataStorage dimensionDataStorage;
    private Set<String> pendingRemove = new HashSet<>();

    public ScopedNbtSavedData(
            String basePath,
            SavedData.Factory<T> factory,
            Path path,
            HolderLookup.Provider provider,
            DataFixer fixer
    ) {
        this.path = path.resolve(basePath);
        if(!this.path.toFile().exists()) {
            if(!this.path.toFile().mkdirs())
                throw new IllegalStateException("Cannot mkdir for NBT Saved Data: " + basePath);
        }
        this.factory = factory;
        this.dimensionDataStorage = new DimensionDataStorage(this.path.toFile(), fixer, provider);
    }
    @Override
    public void save(Path path, HolderLookup.Provider provider) {
        for (String data : pendingRemove) {
            File f = new File(this.path.toFile(), data + ".nbt");
            if(f.exists()) {
                f.delete();
            }
        }

        this.dimensionDataStorage.save();
    }

    public T get(String key) {
        if(this.pendingRemove.contains(key))
            return null;
        return this.dimensionDataStorage.get(this.factory, key);
    }

    public T computeIfAbsent(String key) {
        if(this.pendingRemove.contains(key)) {
            this.pendingRemove.remove(key);
            T data = this.factory.constructor().get();
            this.dimensionDataStorage.set(key, data);
            return data;
        }
        return this.dimensionDataStorage.computeIfAbsent(this.factory, key);
    }

    public List<String> getAllSavedNames() {
        File[] files = this.path.toFile().listFiles();
        if(files == null) return List.of();
        ArrayList<String> list = new ArrayList<>();
        for (File file : files) {
            if(file.isFile() && file.getName().endsWith(".nbt")) {
                String fileName = file.getName();
                String baseName = fileName.substring(0, fileName.length() - 4); // remove .nbt
                list.add(baseName);
            }
        }
        return list;
    }

    public void remove(String data) {
        //noinspection DataFlowIssue
        this.dimensionDataStorage.set(data, null);
        this.pendingRemove.add(data);
    }
}
