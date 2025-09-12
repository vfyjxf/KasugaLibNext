package lib.kasuga.test.registration.minecraft.block_entity;

import lib.kasuga.registration.minecraft.block_entity.renderer.BlockEntityRendererBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;

public class TestBlockEntityRenderer implements BlockEntityRenderer<TestCustomBlockEntity>, BlockEntityRendererBuilder<TestCustomBlockEntity> {
    
    public TestBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // Test constructor
    }

    @Override
    public void render(TestCustomBlockEntity blockEntity, float partialTick, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int light, int overlay) {
        // Simple test render - do nothing for testing
    }

    @Override
    public BlockEntityRenderer<TestCustomBlockEntity> build(BlockEntityRendererProvider.Context context) {
        return new TestBlockEntityRenderer(context);
    }
}
