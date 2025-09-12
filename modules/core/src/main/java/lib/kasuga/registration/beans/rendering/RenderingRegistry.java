package lib.kasuga.registration.beans.rendering;


import lib.kasuga.registration.minecraft.block_entity.renderer.BlockEntityRendererBuilder;
import lib.kasuga.registration.minecraft.entity.renderer.EntityRendererBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public interface RenderingRegistry {
    public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<T> blockEntityType, Supplier<BlockEntityRendererBuilder<T>> supplier);

    <E extends Entity> void registerEntityRenderer(EntityType<E> validEntity, Supplier<EntityRendererBuilder<E>> provider);
}
