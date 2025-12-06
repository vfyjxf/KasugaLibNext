package lib.kasuga.registration.minecraft_old.block_entity.renderer;

import lib.kasuga.KasugaLib;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.beans.rendering.RenderingRegistry;
import lib.kasuga.registration.core.RegisterContext;
import lib.kasuga.registration.stages.RegistrationStage;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;

public class BlockEntityRendererReg<B extends BlockEntity> extends Reg<BlockEntityRendererReg<B>, Void> implements BlockEntityRendererConfigurations<BlockEntityRendererReg<B>> {

    private final Supplier<BlockEntityRendererBuilder<B>> provider;

    public BlockEntityRendererReg(Supplier<BlockEntityRendererBuilder<B>> provider) {
        super();
        this.provider = provider;
    }

    @Override
    public void register(RegisterContext<?> context) {
        super.register(context);
        context.onStage(RegistrationStage.BAKING_COMPLETE, (ctx)->{
            Collection<BlockEntityType<?>> validBlockEntities = this.transform(BlockEntityRendererModifiers.BLOCK_ENTITIES, new HashSet<>());
            for (BlockEntityType<?> validBlockEntity : validBlockEntities) {
                //noinspection unchecked
                KasugaLib.getContext().getBean(RenderingRegistry.class)
                        .registerBlockEntityRenderer((BlockEntityType<B>) validBlockEntity, provider);
            }
        });
    }

    @Override
    public Void getEntry() {
        throw new IllegalStateException("BlockEntityRendererReg does not have an entry");
    }

    public void block() {}
}
