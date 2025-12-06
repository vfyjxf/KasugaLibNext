package lib.kasuga.create.content.train.graph;

import net.minecraft.nbt.CompoundTag;

public interface EdgeExtraPayload {
    CompoundTag write();

    EdgeExtraPayloadType<?> getType();
}
