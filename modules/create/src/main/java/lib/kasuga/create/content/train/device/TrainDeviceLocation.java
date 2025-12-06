package lib.kasuga.create.content.train.device;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public record TrainDeviceLocation(
   UUID trainId,
    int carriageIndex,
   BlockPos position
) {
    public static TrainDeviceLocation read(CompoundTag tag) {
        return new TrainDeviceLocation(
                tag.getUUID("TrainId"),
                tag.getInt("CarriageIndex"),
                BlockPos.of(tag.getLong("Position"))
        );
    }

    public CompoundTag write(CompoundTag tag) {
        tag.putUUID("TrainId", trainId);
        tag.putInt("CarriageIndex", carriageIndex);
        tag.putLong("Position", position.asLong());
        return tag;
    }
}
