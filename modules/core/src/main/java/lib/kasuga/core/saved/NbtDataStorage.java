package lib.kasuga.core.saved;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.saveddata.SavedData;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NbtDataStorage<T extends SavedData> {
    private Map<String, T> cache = new HashMap<>();
    private File directory;
    private HolderLookup.Provider provider;

    public NbtDataStorage(File directory, HolderLookup.Provider provider) {
        this.directory = directory;
        this.provider = provider;
    }


}
