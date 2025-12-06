package lib.kasuga.create.content.train.signal;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class CustomTrackSegment {
    protected UUID segmentId;

    public CustomTrackSegment(UUID segmentId){
        this.segmentId = segmentId;
    }

    public CompoundTag write(){
        return new CompoundTag();
    }

    public void read(CompoundTag tag){

    }

    public void onEarlyGlobalTick() {}
}
