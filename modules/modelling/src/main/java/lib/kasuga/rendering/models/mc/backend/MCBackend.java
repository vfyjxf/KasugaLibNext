package lib.kasuga.rendering.models.mc.backend;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lib.kasuga.rendering.models.mc.backend.data_type.KasugaShaderInstance;
import lib.kasuga.rendering.models.mc.compat.iris.IrisCompat;
import lib.kasuga.rendering.models.uml.backend.Backend;
import lib.kasuga.rendering.models.uml.bridge.Bridge;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.util.Objects;

public class MCBackend extends Backend<Bridge, KsgVertexBuffer, MCBackendContext> {



    @Override
    public void render(KsgVertexBuffer renderable, MCBackendContext context) {
        PoseStack poseStack = context.getPoseStack();
        poseStack.pushPose();

        BufferBuilder builder = (BufferBuilder) context.getVertexConsumer();
        KasugaShaderInstance shader = null;
        if (!IrisCompat.isUsingShaderPack()) {
            RenderSystem.setShader(() -> RenderState.UML_SHADER_INSTANCE);
            shader  = (KasugaShaderInstance) RenderSystem.getShader();
        }
        renderable.upload(builder, poseStack.last(), shader, 1f, 1f, LightTexture.FULL_BLOCK, OverlayTexture.NO_OVERLAY, true);

        poseStack.popPose();
    }
}
