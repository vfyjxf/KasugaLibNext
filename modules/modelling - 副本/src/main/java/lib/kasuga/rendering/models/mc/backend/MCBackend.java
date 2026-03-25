package lib.kasuga.rendering.models.mc.backend;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lib.kasuga.rendering.models.uml.backend.Backend;
import lib.kasuga.rendering.models.uml.bridge.Bridge;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;

public class MCBackend extends Backend<Bridge, BakedQuad, MCBackendContext> {



    @Override
    public void render(BakedQuad[] renderable, MCBackendContext context) {
        if (!(context.getVertexConsumer() instanceof KsgBufferBuilder consumer)) return;
        PoseStack poseStack = context.getPoseStack();
        poseStack.pushPose();

        for (BakedQuad quad : renderable) {
            consumer.putBulkData(context.getPoseStack().last(),
                    quad,
                    new float[]{1f, 1f, 1f, 1f},
                    new int[]{LightTexture.FULL_BLOCK, LightTexture.FULL_BLOCK, LightTexture.FULL_BLOCK, LightTexture.FULL_BLOCK},
                    0, true);
        }
        poseStack.popPose();
    }
}
