package lib.kasuga.rendering.models.mc.backend.data_type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import lib.kasuga.rendering.models.uml.math.Transform;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.io.IOException;

public class KasugaShaderInstance extends ShaderInstance {

    private static final Matrix4f IDENTITY_M4F = new Matrix4f().identity();
    private static final Matrix3f IDENTITY_M3F = new Matrix3f().identity();

    @Getter
    private float emissiveStrength = 1.0f;

    @Setter
    private @Nullable PoseStack.Pose currentPose;


    public KasugaShaderInstance(ResourceProvider p_173336_, ResourceLocation shaderLocation, VertexFormat p_173338_) throws IOException {
        super(p_173336_, shaderLocation, p_173338_);
    }

    public void setEmissiveStrength(float emissiveStrength) {
        this.emissiveStrength = Math.clamp(emissiveStrength, 0f, 1f);
    }

    public void disableEmissive() {
        setEmissiveStrength(0f);
    }

    public void disablePose() {
        this.currentPose = null;
    }

    protected void applyAdditionalData() {
        this.safeGetUniform("EmissiveStrength").set(this.emissiveStrength);
        if (currentPose != null) {
            this.safeGetUniform("ModelPoseMat").set(this.currentPose.pose());
            this.safeGetUniform("ModelNormalMat").set(this.currentPose.normal());
        } else {
            this.safeGetUniform("ModelPoseMat").set(IDENTITY_M4F);
            this.safeGetUniform("ModelNormalMat").set(IDENTITY_M3F);
        }
    }

    @Override
    public void apply() {
        applyAdditionalData();
        super.apply();
    }
}
