package lib.kasuga.create.registration.edge_data;

import lib.kasuga.create.content.train.graph.EdgeExtraPayload;
import lib.kasuga.create.content.train.graph.EdgeExtraPayloadRegistry;
import lib.kasuga.create.content.train.graph.EdgeExtraPayloadType;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.RegisterContext;
import lib.kasuga.registration.core.ResourceLocationModifiers;
import lib.kasuga.registration.stages.RegistrationStage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.Supplier;

public class EdgeDataReg<T extends EdgeExtraPayload> extends Reg<EdgeDataReg<T>, EdgeExtraPayloadType<T>> {
    private final EdgeExtraPayloadType.Builder<T> entrySupplier;
    private final String id;
    private EdgeExtraPayloadType<T> entry;

    public EdgeDataReg(String registrationKey, Supplier<T> entrySupplier, Function<CompoundTag, T> reader) {
        super();
        this.id = registrationKey;
        this.entrySupplier = new EdgeExtraPayloadType.Builder<>(entrySupplier, reader);
    }

    @Override
    public void register(RegisterContext<?> context) {
        context.onStage(RegistrationStage.REGISTER_EVENT, (ctx)->{
            if(entry == null){
                this.entry = entrySupplier.build();
                EdgeExtraPayloadRegistry.register(transform(ResourceLocationModifiers.ID, ResourceLocation.withDefaultNamespace(id)), entry);
            }
        });
    }

    @Override
    public EdgeExtraPayloadType<T> getEntry() {
        return this.entry;
    }
}
