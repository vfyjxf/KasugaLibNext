package lib.kasuga.create.content.train.device;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import lib.kasuga.structure.Pair;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Optional;

public class TrainDeviceManager {
    private final Train train;

    public TrainDeviceManager(Train train) {
        this.train = train;
        this.systems.putAll(TrainDeviceRegistry.createInitialContextDevices(this));
    }

    private boolean cachedCancelSlowdown = false;

    public static Pair<TrainDeviceManager, TrainDeviceLocation> getManager(MovementContext context) {
        if(context.contraption == null || !(context.contraption.entity instanceof CarriageContraptionEntity entity))
            return null;
        return Pair.of(
                getManager(entity.getCarriage().train),
                new TrainDeviceLocation(
                        entity.trainId,
                        entity.carriageIndex,
                        context.localPos
                ));
    }

    public static Pair<TrainDeviceManager, TrainDeviceLocation> getManager(AbstractContraptionEntity contraption, BlockPos position) {
        if(!(contraption instanceof CarriageContraptionEntity))
            return null;
        CarriageContraptionEntity entity = ((CarriageContraptionEntity)contraption);
        return Pair.of(
                getManager(entity.getCarriage().train),
                new TrainDeviceLocation(
                        entity.trainId,
                        entity.carriageIndex,
                        position
                ));
    }

    public static TrainDeviceManager getManager(Train train) {
        return ((TrainDeviceProvider) train).kasugaLib$getDeviceManager();
    }

    protected HashMap<TrainDeviceSystemType<?>, TrainDeviceSystem> systems = new HashMap<>();

    public void read(CompoundTag tag) {
        this.systems.clear();
        ListTag systemList = tag.getList("TrainDeviceSystems", 10);
        for (int i = 0; i < systemList.size(); i++) {
            CompoundTag systemTag = systemList.getCompound(i);
            String typeString = systemTag.getString("Type");
            ResourceLocation typeId = ResourceLocation.tryParse(typeString);
            if (typeId == null) continue;
            TrainDeviceSystemType<?> type = TrainDeviceRegistry.get(typeId);
            if (type == null) continue;
            TrainDeviceSystem system = type.create(this);
            system.read(systemTag);
            systems.put(type, system);
        }
        updateCache();
    }

    public void write(CompoundTag tag) {
        ListTag systemList = new ListTag();
        for (TrainDeviceSystemType<?> type : systems.keySet()) {
            CompoundTag systemTag = new CompoundTag();
            CompoundTag data = new CompoundTag();
            systems.get(type).write(systemTag);
            data.put("Data", systemTag);
            systemTag.putString("Type", TrainDeviceRegistry.getKey(type).toString());
            systemList.add(systemTag);
        }
        tag.put("TrainDeviceSystems", systemList);
    }

    public void tick(Level level) {
        for (TrainDeviceSystem system : systems.values()) {
            system.tick(level);
        }
    }

    public Optional<Double> beforeSpeed(){
        for (TrainDeviceSystem system : systems.values()) {
            Optional<Double> newSpeed = system.beforeSpeed();
            if(newSpeed.isPresent())
                return newSpeed;
        }
        return Optional.empty();
    }

    public void notifySpeed(double speed) {
        for (TrainDeviceSystem system : systems.values()) {
            system.notifySpeed(speed);
        }
    }

    public boolean notifySignalFront(Double distance, Pair<TrackEdgePoint, Couple<TrackNode>> pair) {
        for (TrainDeviceSystem system : systems.values()) {
            if(system.notifySignalFront(distance, pair)){
                return true;
            }
        }
        return false;
    }

    public boolean notifySignalBack(Double distance, Pair<TrackEdgePoint, Couple<TrackNode>> pair) {
        for (TrainDeviceSystem system : systems.values()) {
            if(system.notifySignalBack(distance, pair)){
                return true;
            }
        }
        return false;
    }



    public <T extends TrainDeviceSystem> T getSystem(TrainDeviceSystemType<T> type) {
        if(!systems.containsKey(type)) {
            return null;
        }
        return (T) systems.get(type);
    }

    public <T extends TrainDeviceSystem> T getOrCreateSystem(TrainDeviceSystemType<T> type) {
        if(!systems.containsKey(type)) {
            T system = type.create(this);
            systems.put(type, system);
            updateCache();
            return system;
        }
        return (T) systems.get(type);
    }

    public boolean cancelSlowdown(){
        return cachedCancelSlowdown;
    }

    public void updateCache(){
        cachedCancelSlowdown = false;
        for (TrainDeviceSystem system : systems.values()) {
            cachedCancelSlowdown |= system.cancelSlowdown();
        }
    }

    public void notifyDistance(double distance) {
        for (TrainDeviceSystem system : systems.values()) {
            system.notifyDistance(distance);
        }
    }

    public Train getTrain() {
        return train;
    }

    public void notifyDisassemble(Direction assemblyDirection, BlockPos pos) {
        for (TrainDeviceSystem system : systems.values()) {
            system.notifyDisassemble(assemblyDirection, pos);
        }
    }

    public void notifySignalCollection() {
        for (TrainDeviceSystem system : systems.values()) {
            system.notifySignalCollection();
        }
    }

    public void earlyTick(Level level) {
        for (TrainDeviceSystem system : systems.values()) {
            system.earlyTick(level);
        }
    }

    public void onSegmentUpdated(ResourceLocation segmentType) {
        for (TrainDeviceSystem system : systems.values()) {
            system.onSegmentUpdated(segmentType);
        }
    }
}
