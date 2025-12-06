package lib.kasuga.create.content.train.device;

import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import lib.kasuga.structure.Pair;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class TrainDeviceSystem {

    protected final TrainDeviceManager manager;
    private boolean initialized = false;

    public TrainDeviceSystem(TrainDeviceManager manager) {
        this.manager = manager;
    }
    public void write(CompoundTag systemTag) {}
    public void read(CompoundTag systemTag) {}

    public void tick(Level level) {
        if(!initialized && !level.isClientSide){
            initializeServer(level);
            initialized = true;
        }
    }

    public Optional<Double> beforeSpeed() {
        return Optional.empty();
    }

    public void notifySpeed(double speed) {}

    public boolean notifySignalBack(Double distance, Pair<TrackEdgePoint, Couple<TrackNode>> pair) {
        return false;
    }

    public boolean notifySignalFront(Double distance, Pair<TrackEdgePoint, Couple<TrackNode>> pair) {
        return false;
    }

    public boolean cancelSlowdown() {
        return false;
    }

    public void notifyDistance(double distance) {}

    public void initializeServer(Level level) {}

    public void notifyDisassemble(Direction assemblyDirection, BlockPos pos) {}

    public TrainDeviceManager getManager() {
        return manager;
    }

    public void notifySignalCollection() {}

    public void earlyTick(Level level) {}

    public void onSegmentUpdated(ResourceLocation segmentType) {}
}
