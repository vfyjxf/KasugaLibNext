package lib.kasuga.create.content.train.graph;

import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import lib.kasuga.create.content.train.signal.BoundarySegmentRegistry;
import lib.kasuga.create.content.train.signal.CustomTrackSegment;
import lib.kasuga.create.content.train.signal.ResourcePalette;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class RailwayData extends SavedData {

    public RailwayData() {}


    HashMap<ResourceLocation, HashMap<UUID, CustomTrackSegment>> segmentInstances = new HashMap<>();

    public HashMap<UUID, GraphExtraData> extraData = new HashMap<>();

    public void createExtraData(UUID graphId){
        this.extraData.computeIfAbsent(graphId, (id)->new GraphExtraData());
    }

    public void removeExtraData(UUID graphId){
        this.extraData.remove(graphId);
    }


    public GraphExtraData withGraph(TrackGraph target) {
        if(this.extraData.containsKey(target.id)){
            return this.extraData.get(target.id);
        }else{
            createExtraData(target.id);
            return this.extraData.get(target.id);
        }
    }


    public void syncExtraData(Set<UUID> uuids) {
        Set<UUID> keySets = new HashSet<>(extraData.keySet());
        HashSet<UUID> shouldAddSets = new HashSet<>();
        for (UUID uuid : uuids) {
            if(keySets.contains(uuid)){
                keySets.remove(uuid);
            }else{
                shouldAddSets.add(uuid);
            }
        }
        shouldAddSets.forEach(this::createExtraData);
        keySets.forEach(this::removeExtraData);
        setDirty();
    }

    public CompoundTag write(DimensionPalette dimensions, ResourcePalette resourcePalette) {
        CompoundTag tag = new CompoundTag();
        ListTag extraDataTags = new ListTag();
        for (Map.Entry<UUID, GraphExtraData> entry : this.extraData.entrySet()) {
            CompoundTag graphExtraDataTag = new CompoundTag();
            graphExtraDataTag.putUUID("Id", entry.getKey());
            graphExtraDataTag.put("Data", entry.getValue().write(dimensions, resourcePalette));
            extraDataTags.add(graphExtraDataTag);
        }
        tag.put("ExtraData", extraDataTags);

        ListTag segmentListTag = new ListTag();
        for (Map.Entry<ResourceLocation, HashMap<UUID, CustomTrackSegment>> entry : segmentInstances.entrySet()) {
            for (Map.Entry<UUID, CustomTrackSegment> segmentEntry : entry.getValue().entrySet()) {
                CompoundTag segmentTag = new CompoundTag();
                segmentTag.putUUID("FeatureId", segmentEntry.getKey());
                segmentTag.putString("FeatureName", entry.getKey().toString());
                segmentTag.put("Data", segmentEntry.getValue().write());
                segmentListTag.add(segmentTag);
            }
        }
        tag.put("Segments", segmentListTag);

        return tag;
    }

    public void read(CompoundTag compoundTag, DimensionPalette dimensions, ResourcePalette resourcePalette) {
        setDirty();
        extraData.clear();
        ListTag extraDataTags = compoundTag.getList("ExtraData", Tag.TAG_COMPOUND);
        for(int i=0;i<extraDataTags.size();i++){
            CompoundTag tag = extraDataTags.getCompound(i);
            UUID id = tag.getUUID("Id");
            GraphExtraData extraData = this.extraData.computeIfAbsent(id, (x)->new GraphExtraData());
            extraData.read(tag.getCompound("Data"), dimensions, resourcePalette);
        }

        ListTag segmentListTag = compoundTag.getList("Segments", ListTag.TAG_COMPOUND);
        for(int i=0;i<segmentListTag.size();i++){
            CompoundTag segmentTag = segmentListTag.getCompound(i);
            ResourceLocation featureName = ResourceLocation.tryParse(segmentTag.getString("FeatureName"));
            //@TODO:Validate featureName
            UUID featureId = segmentTag.getUUID("FeatureId");
            CustomTrackSegment segment = BoundarySegmentRegistry.createSegmentByFeatureName(featureName, featureId);
            segment.read(segmentTag.getCompound("Data"));
            addSegment(featureName, featureId, segment);
        }
    }

    public void addSegment(ResourceLocation featureName, UUID featureId, CustomTrackSegment segment){
        segmentInstances.computeIfAbsent(featureName, (i)->new HashMap<>()).put(featureId, segment);
    }

    public void removeSegment(ResourceLocation featureName, UUID featureId) {
        if(!segmentInstances.containsKey(featureName))
            return;
        segmentInstances.get(featureName).remove(featureId);
    }

    public CustomTrackSegment getSegment(ResourceLocation featureName, UUID featureId) {
        if(!segmentInstances.containsKey(featureName))
            return null;
        return segmentInstances.get(featureName).get(featureId);
    }

    public boolean hasSegment(ResourceLocation featureName, UUID featureId){
        if(!segmentInstances.containsKey(featureName))
            return false;
        return segmentInstances.get(featureName).containsKey(featureId);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ResourcePalette palette = new ResourcePalette();
        DimensionPalette dimensions = new DimensionPalette();
        compoundTag.put("Data", this.write(dimensions, palette));
        dimensions.write(compoundTag);
        palette.write(compoundTag);
        return compoundTag;
    }

    public static RailwayData create() {
        return new RailwayData();
    }

    public static RailwayData load(CompoundTag tag, HolderLookup.Provider provider) {
        RailwayData railwayData = new RailwayData();
        ResourcePalette palette = ResourcePalette.read(tag);
        DimensionPalette dimensions = DimensionPalette.read(tag);
        railwayData.read(tag.getCompound("Data"), dimensions, palette);
        return railwayData;
    }

    public void onEarlyGlobalTick(Level level) {
        for (HashMap<UUID, CustomTrackSegment> value : segmentInstances.values()) {
            for (CustomTrackSegment segment : value.values()) {
                segment.onEarlyGlobalTick();
            }
        }
    }
}
