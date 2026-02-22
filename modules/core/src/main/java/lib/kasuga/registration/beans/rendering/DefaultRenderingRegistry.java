package lib.kasuga.registration.beans.rendering;

import com.mojang.logging.LogUtils;
import jakarta.inject.Singleton;
import lib.kasuga.content.document.DocumentComponentRegistries;
import lib.kasuga.content.document.DocumentComponentType;
import lib.kasuga.registration.kasuga.document.DocumentComponentRendererSupplier;
import lib.kasuga.registration.minecraft.block_entity.BlockEntityRendererBuilder;
import lib.kasuga.registration.minecraft.entity.EntityRendererBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Singleton
public class DefaultRenderingRegistry implements RenderingRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    @Override
    public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<T> blockEntityType, Supplier<BlockEntityRendererBuilder<T>> supplier) {
        LOGGER.debug("Ignored server-side registration of BlockEntity " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKeyOrNull(blockEntityType));
    }

    @Override
    public <E extends Entity> void registerEntityRenderer(EntityType<E> validEntity, Supplier<EntityRendererBuilder<E>> provider) {
        LOGGER.debug("Ignored server-side registration of Entity " + BuiltInRegistries.ENTITY_TYPE.getKeyOrNull(validEntity));
    }

    @Override
    public <T, S extends DocumentComponentType<T>> void registerDocumentComponentRenderer(S componentType, Supplier<DocumentComponentRendererSupplier<T>> rendererSupplier) {
        LOGGER.debug("Ignored server-side registration of Entity " + DocumentComponentRegistries.DOCUMENT_COMPONENT_REGISTRY.getKeyOrNull(componentType));
    }
}
