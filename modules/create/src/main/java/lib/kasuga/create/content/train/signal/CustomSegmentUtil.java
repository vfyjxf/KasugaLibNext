package lib.kasuga.create.content.train.signal;

import com.simibubi.create.content.trains.graph.EdgeData;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lib.kasuga.create.content.train.graph.EdgeExtraData;
import lib.kasuga.create.content.train.graph.GraphExtraData;
import lib.kasuga.create.content.train.graph.RailwayData;
import lib.kasuga.create.content.train.graph.RailwayManager;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

@Singleton()
public class CustomSegmentUtil {
    @Inject() private RailwayManager railwayManager;
    public UUID getSegmentIdAt(
            TrackGraph graph,
            TrackEdge edge,
            EdgeData edgeData,
            EdgeExtraData extraData,
            EdgePointType<? extends CustomBoundary> boundaryType,
            ResourceLocation featureName,
            double position
    ){
        if(!extraData.hasCustomBoundaryInThisEdge(featureName)){
            return getEffectiveCircuit(graph,extraData,featureName);
        }
        CustomBoundary firstCircuit = edgeData.next(boundaryType,0);
        if(firstCircuit == null){
            return null;
        }
        UUID current = firstCircuit.getGroupId(false);

        for (TrackEdgePoint trackEdgePoint : edgeData.getPoints()) {
            if (!(trackEdgePoint instanceof CustomBoundary customBoundary))
                continue;
            if (customBoundary.getLocationOn(edge) >= position)
                return current;
            current = customBoundary.getGroupId(true);
        }
        return current;
    }

    public CustomTrackSegment getSegment(
            TrackGraph graph,
            TrackEdge edge,
            EdgePointType<? extends CustomBoundary> boundaryType,
            double position
    ){
        RailwayData railwayData = railwayManager.getData();
        GraphExtraData extraData = railwayData.withGraph(graph);

        ResourceLocation featureName = BoundarySegmentRegistry.getFeatureName(boundaryType);
        UUID segmentId = getSegmentIdAt(
                graph,
                edge,
                edge.getEdgeData(),
                extraData.getOrComputeEdgeData(edge),
                boundaryType,
                featureName,
                position
        );

        return railwayData.getSegment(featureName, segmentId);
    }

    public static UUID getEffectiveCircuit(TrackGraph graph, EdgeExtraData extraData, ResourceLocation featureName){
        return !extraData.hasBoundaryFeature(featureName) ? null : (
                extraData.getBoundaryFeature(featureName) == EdgeExtraData.passiveBoundaryGroup ?
                        graph.id : extraData.getBoundaryFeature(featureName)
        );
    }
}
