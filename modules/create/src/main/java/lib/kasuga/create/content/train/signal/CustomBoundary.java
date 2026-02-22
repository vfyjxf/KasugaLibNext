package lib.kasuga.create.content.train.signal;

import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.signal.SingleBlockEntityEdgePoint;
import lib.kasuga.KasugaLib;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class CustomBoundary extends SingleBlockEntityEdgePoint {
    private final CustomTrackSegmentPropagator propagator;
    Map<Boolean, UUID> sidedSegments = new HashMap<>();
    Map<Boolean, Boolean> dirty = new HashMap<>();

    protected CustomBoundary(){
        this.propagator = KasugaLib.getBean(CustomTrackSegmentPropagator.class);
        dirty.put(false,true);
        dirty.put(true,true);
    }

    @Override
    public void onRemoved(TrackGraph graph) {
        super.onRemoved(graph);
        propagator.onRemoved(graph, this);
    }

    public void setSegment(boolean direction, UUID segmentId) {
        sidedSegments.put(direction, segmentId);
    }

    public void setSegmentAndUpdate(boolean direction, UUID segmentId){
        sidedSegments.put(direction, segmentId);
        dirty.put(direction, false);
    }

    public void markDirty(boolean direction) {
        dirty.put(direction, true);
    }

    public UUID getGroupId(boolean direction) {
        return sidedSegments.get(direction);
    }

    @Override
    public void tick(TrackGraph graph, boolean preTrains) {
        for (boolean i : Iterate.trueAndFalse){
            if(shouldUpdate(i)){
                dirty.put(i, false);
                propagator.propagate(graph,this,i);
                onPropagateFinished(i);
            }
        }
    }

    protected void onPropagateFinished(boolean direction) {}

    public boolean shouldUpdate(boolean direction) {
        return dirty.get(direction);
    }

    @Override
    public void write(CompoundTag nbt, HolderLookup.Provider registries, DimensionPalette dimensions) {
        super.write(nbt, registries, dimensions);
        for (boolean i : Iterate.trueAndFalse){
            if(shouldUpdate(i)){
               nbt.putBoolean("ShouldUpdate" + (i ? "Front" : "Back"), true);
            }
            nbt.putUUID("SignalGroup"+ (i ? "Front" : "Back"), sidedSegments.get(i));
        }
    }

    @Override
    public void read(CompoundTag nbt, HolderLookup.Provider provider, boolean migration, DimensionPalette dimensions) {
        super.read(nbt, provider, migration, dimensions);
        for (boolean i : Iterate.trueAndFalse){
            this.dirty.put(i,nbt.getBoolean("ShouldUpdate" + (i ? "Front" : "Back")));
            this.sidedSegments.put(i, nbt.getUUID("SignalGroup"+ (i ? "Front" : "Back")));
        }
    }

    public void clearDirty() {
        this.dirty.put(false, false);
        this.dirty.put(true, false);
    }
}
