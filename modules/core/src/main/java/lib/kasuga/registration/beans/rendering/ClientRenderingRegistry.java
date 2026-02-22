package lib.kasuga.registration.beans.rendering;

import com.mojang.logging.LogUtils;
import io.micronaut.context.annotation.Replaces;
import jakarta.inject.Singleton;
import lib.kasuga.content.document.DocumentComponentType;
import lib.kasuga.content.document.DocumentItemRenderer;
import lib.kasuga.inject.class_loader.BeanOnlyIn;
import lib.kasuga.inject.auto_configure.Configurable;
import lib.kasuga.registration.kasuga.document.DocumentComponentRendererSupplier;
import lib.kasuga.registration.minecraft.block_entity.BlockEntityRendererBuilder;
import lib.kasuga.registration.minecraft.entity.EntityRendererBuilder;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Singleton
@Replaces(DefaultRenderingRegistry.class)
@BeanOnlyIn.Client
public class ClientRenderingRegistry implements RenderingRegistry, Configurable {
    protected Logger LOGGER = LogUtils.getLogger();
    protected ModConfigSpec.BooleanValue IGNORE_BLOCK_ENTITY_RENDERER;

    @Override
    public void configureClient(ModConfigSpec.Builder builder) {
        IGNORE_BLOCK_ENTITY_RENDERER = builder
                .comment("Disable the registration of block entity renderers.")
                .define("ignore_block_entity_renderer", false);
    }

    @Override
    public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<T> blockEntityType, Supplier<BlockEntityRendererBuilder<T>> supplier) {
        if(IGNORE_BLOCK_ENTITY_RENDERER.getAsBoolean()){
            LOGGER.warn("Registration BER has disabled by configuration: " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType));
            return;
        }
        BlockEntityRenderers.register(blockEntityType, supplier.get()::build);
    }

    @Override
    public <E extends Entity> void registerEntityRenderer(EntityType<E> validEntity, Supplier<EntityRendererBuilder<E>> provider) {
        EntityRenderers.register(validEntity, provider.get()::build);
    }

    @Override
    public <T, S extends DocumentComponentType<T>> void registerDocumentComponentRenderer(S componentType, Supplier<DocumentComponentRendererSupplier<T>> rendererSupplier) {
        DocumentItemRenderer.registerComponentRenderer(componentType, rendererSupplier.get().get());
    }
}
