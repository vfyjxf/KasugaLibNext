package lib.kasuga.content.device;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public abstract class DeviceAttribute {
    public abstract DeviceAttributeType<?> getType();

    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        return compoundTag;
    }
}
