package lib.kasuga.registration.minecraft_old.block_entity;

import lib.kasuga.registration.core.IChildrenConfiguration;
import lib.kasuga.registration.core.IModifierConfigure;
import lib.kasuga.registration.minecraft_old.block.ChildrenUtils;
import lib.kasuga.registration.minecraft_old.block_entity.renderer.BlockEntityRendererBuilder;
import lib.kasuga.registration.minecraft_old.block_entity.renderer.BlockEntityRendererModifiers;
import lib.kasuga.registration.minecraft_old.block_entity.renderer.BlockEntityRendererReg;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface BlockChildrenConfigurations<S> extends IChildrenConfiguration<S>, IModifierConfigure<S> {
    public default <B extends BlockEntity> S withBlockEntityRenderer(Supplier<BlockEntityRendererBuilder<B>> provider) {
        BlockEntityRendererReg<B> blockEntityRendererReg = new BlockEntityRendererReg<>(provider);
        withBlockEntityRenderer(blockEntityRendererReg);
        return addChild(blockEntityRendererReg);
    }

    public default S withBlockEntityRenderer(BlockEntityRendererReg<?> rendererReg) {
        rendererReg.configure(BlockEntityRendererModifiers.BLOCK_ENTITY_BY_SUPPLIER.apply(()->{
            ArrayList<lib.kasuga.registration.minecraft_old.block_entity.BlockEntityReg<?>> l = new ArrayList<>();
            ChildrenUtils.traverse(this, (r)->{
                if(r instanceof BlockEntityReg<?> br) {
                    l.add(br);
                }
            });
            return l.stream().map(i -> ((BlockEntityType<?>) (i.getEntry()))).collect(Collectors.toList());
        }));
        return self();
    }
}
