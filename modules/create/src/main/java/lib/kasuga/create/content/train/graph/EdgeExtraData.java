package lib.kasuga.create.content.train.graph;

import com.simibubi.create.content.trains.graph.TrackGraph;
import lib.kasuga.create.content.train.signal.ResourcePalette;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EdgeExtraData {
    public static final UUID passiveBoundaryGroup = UUID.fromString("00000000-0000-0000-0000-000000000000");
    HashMap<ResourceLocation, UUID> customBoundaryGroups = new HashMap<>();
    HashMap<ResourceLocation, EdgeExtraPayload> payload = new HashMap<>();
    public CompoundTag write(ResourcePalette resourcePalette) {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (Map.Entry<ResourceLocation, UUID> entry : customBoundaryGroups.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt("Id", resourcePalette.encode(entry.getKey()));
            if(entry.getValue() == null){
                entryTag.putBoolean("NullValue", true);
            }else{
                entryTag.putUUID("Value", entry.getValue());
            }
            listTag.add(entryTag);
        }
        tag.put("BoundaryGroups", listTag);
        ListTag payloadTag = new ListTag();
        for (Map.Entry<ResourceLocation, EdgeExtraPayload> entry : payload.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt("Id", resourcePalette.encode(entry.getKey()));
            entryTag.put("Data", entry.getValue().write());
            payloadTag.add(entryTag);
        }
        tag.put("Payload", payloadTag);
        return tag;
    }

    public void read(CompoundTag data, ResourcePalette resourcePalette) {
        ListTag tag = data.getList("BoundaryGroups", Tag.TAG_COMPOUND);
        for(int i=0;i<tag.size();i++){
            CompoundTag entryTag = tag.getCompound(i);
            UUID value;
            if(!entryTag.getBoolean("NullValue")){
                value = entryTag.getUUID("Value");
            } else value = null;
            customBoundaryGroups.put(
                    resourcePalette.decode(entryTag.getInt("Id")),
                    value
            );
        }

        ListTag payloadTag = data.getList("Payload", Tag.TAG_COMPOUND);
        for(int i=0;i<payloadTag.size();i++){
            CompoundTag entryTag = payloadTag.getCompound(i);
            EdgeExtraPayloadType<?> type = EdgeExtraPayloadRegistry.get(resourcePalette.decode(entryTag.getInt("Id")));
            if(type == null)
                continue;
            EdgeExtraPayload payload = (EdgeExtraPayload) type.read(entryTag.getCompound("Data"));
            this.payload.put(resourcePalette.decode(entryTag.getInt("Id")), payload);
        }
    }

    public boolean hasBoundaryFeature(ResourceLocation featureName) {
        return !customBoundaryGroups.containsKey(featureName) || customBoundaryGroups.get(featureName)!=null;
    }

    public boolean hasCustomBoundaryInThisEdge(ResourceLocation featureName){
        return customBoundaryGroups.containsKey(featureName) && customBoundaryGroups.get(featureName)==null;
    }

    public void setBoundaryFeature(ResourceLocation featureName, UUID segmentId) {
        if(segmentId == passiveBoundaryGroup){
            setBoundaryFeaturePassive(featureName);
            return;
        }
        customBoundaryGroups.put(featureName, segmentId);
    }

    public UUID getBoundaryFeature(ResourceLocation featureName) {
        return customBoundaryGroups.getOrDefault(featureName, passiveBoundaryGroup);
    }

    public UUID getEffectiveBoundaryFeature(TrackGraph graph, ResourceLocation featureName) {
        UUID id = customBoundaryGroups.get(featureName);
        if(id == null || id == passiveBoundaryGroup){
            return graph.id;
        }
        return id;
    }

    public void setBoundaryFeaturePassive(ResourceLocation featureName) {
        customBoundaryGroups.remove(featureName);
    }

    public void setPayload(EdgeExtraPayload payload) {
        this.payload.put(EdgeExtraPayloadRegistry.getId(payload.getType()), payload);
    }

    public EdgeExtraPayload getPayload(ResourceLocation featureName) {
        return payload.computeIfAbsent(featureName, (x)->{
            EdgeExtraPayloadType<?> type = EdgeExtraPayloadRegistry.get(featureName);
            if(type == null)
                return null;
            return (EdgeExtraPayload) type.create();
        });
    }

    public Optional<EdgeExtraPayload> getPayloadOptional(ResourceLocation featureName) {
        return Optional.ofNullable(payload.get(featureName));
    }
}
