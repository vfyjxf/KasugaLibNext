package lib.kasuga.registration.minecraft.block_entity.renderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityRendererBuilder<T extends BlockEntity> {
    BlockEntityRenderer<T> build(BlockEntityRendererProvider.Context context);
}
