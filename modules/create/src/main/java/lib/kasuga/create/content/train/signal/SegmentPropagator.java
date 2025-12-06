package lib.kasuga.create.content.train.signal;

import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SegmentPropagator {
    public static void walk(
            TrackGraph graph,
            ArrayDeque<Couple<TrackNode>> frontier,
            Predicate<TrackEdge> internalBoundaryCheck,
            Consumer<TrackEdge> onVisitNoBoundaryEdge,
            Consumer<TrackEdge> onVisitBoundaryEdge
    ) {
        Set<TrackEdge> visited = new HashSet<>();
        while (!frontier.isEmpty()) {
            Couple<TrackNode> couple = frontier.removeFirst();

            TrackNode currentNode = couple.getFirst();
            TrackNode prevNode = couple.getSecond();

            for (boolean prev : Iterate.falseAndTrue) {
                for (Map.Entry<TrackNode, TrackEdge> entry : graph.getConnectionsFrom(prev ? prevNode : currentNode).entrySet()) {
                    TrackNode nextNode = entry.getKey();
                    TrackEdge edge = entry.getValue();

                    if (nextNode == prevNode)
                        continue;

                    if (!visited.add(edge))
                        continue;

                    TrackEdge oppositeEdge = graph.getConnectionsFrom(nextNode)
                            .get(currentNode);
                    visited.add(oppositeEdge);

                    if(!internalBoundaryCheck.test(edge) && !internalBoundaryCheck.test(oppositeEdge)){
                        onVisitNoBoundaryEdge.accept(edge);
                        onVisitNoBoundaryEdge.accept(oppositeEdge);
                        frontier.add(Couple.create(nextNode, currentNode));
                    } else {
                        onVisitBoundaryEdge.accept(edge);
                    }
                }
            }
        }
    }
}
