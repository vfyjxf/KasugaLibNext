package lib.kasuga.content.device;

import net.minecraft.nbt.CompoundTag;

public class Device {
    private boolean dirty;

    public void setDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void cleanDirty() {
        this.dirty = false;
    }

    public CompoundTag save(CompoundTag tag) {
        return tag;
    }

    public void load(CompoundTag data) {}

    public DeviceType<?> getType() {
        return null;
    }

    public void acceptMerge(Device fromDevice) {}
}
