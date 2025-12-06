package lib.kasuga.create.registration.edge_point;

import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.content.trains.track.TrackTargetingBlockItem;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.RegisterContext;
import lib.kasuga.registration.core.ResourceLocationModifiers;
import lib.kasuga.registration.stages.RegistrationStage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class TrackEdgePointReg<T extends TrackEdgePoint> extends Reg<TrackEdgePointReg<T>, EdgePointType<T>> {
    private final String id;
    private final Supplier<T> type;
    private EdgePointType<T> delegate;

    public TrackEdgePointReg(String id, Supplier<T> type) {
        super();
        this.id = id;
        this.type = type;
    }

    @Override
    public void register(RegisterContext<?> context) {
        context.onStage(RegistrationStage.REGISTER_EVENT, (ctx) -> {
            if(delegate == null) {
                delegate = EdgePointType.register(transform(ResourceLocationModifiers.ID, ResourceLocation.withDefaultNamespace(id)), type);
            }
        });
    }

    @Override
    public EdgePointType<T> getEntry() {
        return delegate;
    }

    public BiFunction<Block, Item.Properties, BlockItem> getBlockItemFactory() {
        return TrackTargetingBlockItem.ofType(this.delegate)::apply;
    }
}
