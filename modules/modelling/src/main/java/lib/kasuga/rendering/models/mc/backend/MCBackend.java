package lib.kasuga.rendering.models.mc.backend;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lib.kasuga.rendering.models.mc.backend.data_type.KasugaShaderInstance;
import lib.kasuga.rendering.models.mc.compat.iris.IrisCompat;
import lib.kasuga.rendering.models.uml.backend.Backend;
import lib.kasuga.rendering.models.uml.bridge.Bridge;
import lib.kasuga.rendering.models.uml.math.QuaternionHelper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import java.util.Objects;

public class MCBackend extends Backend<Bridge, KsgVertexBuffer, MCBackendContext> {

    private static float yRot = 0f;
    private static BlockPos pos = new BlockPos(0, 0, 0);

    @Override
    public void render(KsgVertexBuffer renderable, MCBackendContext context) {
        PoseStack poseStack = context.getPoseStack();
        poseStack.pushPose();
        poseStack.mulPose(QuaternionHelper.fromXYZAngle(0, yRot, 0, true));
        yRot += 0.1f;

        BufferBuilder builder = (BufferBuilder) context.getVertexConsumer();
        KasugaShaderInstance shader = null;
        if (!IrisCompat.isUsingShaderPack()) {
            RenderSystem.setShader(() -> RenderState.UML_SHADER_INSTANCE);
            shader  = (KasugaShaderInstance) RenderSystem.getShader();
        }
        Level level = context.getLevel();
        LightData lightData = getLightData(level, pos);
        renderable.upload(builder, poseStack.last(), shader, lightData.brightness, 1f, lightData.packedLight, OverlayTexture.NO_OVERLAY, true);

        poseStack.popPose();
    }

    public record LightData(int blockLight, int skyLight, int packedLight, float brightness) {

        @Override
        public int hashCode() {
            return Objects.hash(blockLight, skyLight, packedLight, brightness);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            LightData other = (LightData) obj;
            return blockLight == other.blockLight &&
                    skyLight == other.skyLight &&
                    packedLight == other.packedLight &&
                    Float.compare(other.brightness, brightness) == 0;
        }
    }

    public static LightData getLightData(Level level, BlockPos pos) {
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        int packedLight = LightTexture.pack(blockLight, skyLight);
        float brightness = LightTexture.getBrightness(level.dimensionType(), level.getMaxLocalRawBrightness(pos));
        return new LightData(blockLight, skyLight, packedLight, brightness);
    }
}
