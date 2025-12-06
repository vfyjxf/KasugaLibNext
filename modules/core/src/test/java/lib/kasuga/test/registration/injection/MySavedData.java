package lib.kasuga.test.registration.injection;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;


public class MySavedData extends SavedData {

    public MySavedData(){}
    public MySavedData(CompoundTag compoundTag, HolderLookup.Provider provider) {}


    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        return compoundTag;
    }
}
