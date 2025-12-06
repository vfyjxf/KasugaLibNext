package lib.kasuga.create.content.train.graph;

import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackNode;
import lib.kasuga.create.content.train.signal.BoundarySegmentRegistry;
import lib.kasuga.create.content.train.signal.CustomTrackSegment;
import lib.kasuga.create.content.train.signal.ResourcePalette;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;

import java.util.*;

public class GraphExtraData {
    HashMap<TrackEdgeLocation, EdgeExtraData> edgeExtraData = new HashMap<>();
    public void transfer(
            LevelAccessor level,
            TrackNode node,
            Map<TrackNode, TrackEdge> connections,
            GraphExtraData targetExtraData
    ) {
        for (TrackEdge edge : connections.values()) {
            EdgeExtraData data = removeEdge(edge);
            targetExtraData.addEdge(edge, data);
        }
    }

    public TrackEdgeLocation findLocationOf(EdgeExtraData data){
        for (Map.Entry<TrackEdgeLocation, EdgeExtraData> entry : edgeExtraData.entrySet()) {
            if(entry.getValue() == data){
                return entry.getKey();
            }
        }
        return null;
    }

    public void transferAll(GraphExtraData toOtherExtra) {
        edgeExtraData.forEach(toOtherExtra::addEdge);
        edgeExtraData.clear();
    }

    public void createEdge(TrackEdge edge) {
        edgeExtraData.put(TrackEdgeLocation.fromEdge(edge), new EdgeExtraData());
    }

    public void addEdge(TrackEdgeLocation edge, EdgeExtraData data) {
        edgeExtraData.put(edge, data);
    }

    public void addEdge(TrackEdge edge, EdgeExtraData data) {
        edgeExtraData.put(TrackEdgeLocation.fromEdge(edge), data);
    }

    public EdgeExtraData removeEdge(TrackEdge edge){
        return edgeExtraData.remove(TrackEdgeLocation.fromEdge(edge));
    }

    public void syncWithExternal(Collection<TrackEdge> edges){
        HashSet<TrackEdgeLocation> keySet = new HashSet<>(edgeExtraData.keySet());
        HashSet<TrackEdgeLocation> shouldAdd = new HashSet<>();
        for (TrackEdge edge : edges) {
            TrackEdgeLocation location = TrackEdgeLocation.fromEdge(edge);
            if(keySet.contains(location)){
                keySet.remove(location);
            }else{
                shouldAdd.add(location);
            }
        }
        shouldAdd.forEach((l)->edgeExtraData.put(l, new EdgeExtraData()));
        keySet.forEach((l)->edgeExtraData.remove(l));
    }

    public EdgeExtraData getEdgeData(TrackEdge edge){
        return edgeExtraData.get(TrackEdgeLocation.fromEdge(edge));
    }

    public EdgeExtraData getOrComputeEdgeData(TrackEdge edge) {
        return edgeExtraData.computeIfAbsent(TrackEdgeLocation.fromEdge(edge), (s)->new EdgeExtraData());
    }

    public CompoundTag write(DimensionPalette dimensions, ResourcePalette resourcePalette) {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (Map.Entry<TrackEdgeLocation, EdgeExtraData> entry : edgeExtraData.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put("Location", entry.getKey().write(dimensions));
            entryTag.put("Data", entry.getValue().write(resourcePalette));
            listTag.add(entryTag);
        }
        tag.put("EdgeExtraData", listTag);
        return tag;
    }

    public void read(CompoundTag data, DimensionPalette dimensions, ResourcePalette resourcePalette) {
        ListTag listTag = data.getList("EdgeExtraData", ListTag.TAG_COMPOUND);
        for(int i=0;i<listTag.size();i++){
            CompoundTag entryTag = listTag.getCompound(i);
            TrackEdgeLocation edgeLocation = TrackEdgeLocation.read(entryTag.getCompound("Location"), dimensions);
            EdgeExtraData extraData = edgeExtraData.computeIfAbsent(edgeLocation, (x)->new EdgeExtraData());
            extraData.read(entryTag.getCompound("Data"), resourcePalette);
        }
    }
}
