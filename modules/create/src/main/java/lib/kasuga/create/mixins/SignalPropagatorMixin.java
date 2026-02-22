package lib.kasuga.create.mixins;

import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.SignalPropagator;
import lib.kasuga.KasugaLib;
import lib.kasuga.create.content.train.signal.CustomTrackSegmentPropagator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SignalPropagator.class, remap = false)
public class SignalPropagatorMixin {
    @Inject(method = "notifySignalsOfNewNode", at = @At("TAIL"))
    private static void onNotifySignalsOfNewNode(TrackGraph graph, TrackNode node, CallbackInfo callbackInfo){
        KasugaLib.getBean(CustomTrackSegmentPropagator.class).notifyNewNode(graph, node);
    }
}
