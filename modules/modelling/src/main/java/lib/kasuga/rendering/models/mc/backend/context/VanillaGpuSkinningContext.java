package lib.kasuga.rendering.models.mc.backend.context;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import lib.kasuga.rendering.models.mc.backend.transform.BoneTransformTBO;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VanillaGpuSkinningContext implements GLContext {

    private int previousProgram;
    private int previousArrayBuffer;
    private int previousVertexArray;
    private int previousActiveTexture;
    private int previousTexturesBinding;
    private boolean previousRasterizerDiscard;

    @Getter
    private final RenderType renderType;

    @Getter
    private final BoneTransformTBO boneTransformTBO;

    @Getter
    private final Supplier<VertexBuffer> bufferSupplier;

    @Getter
    @Nullable
    private final Consumer<ShaderInstance> beforeShaderApply;

    public VanillaGpuSkinningContext(RenderType renderType,
                                     BoneTransformTBO boneTransformTBO,
                                     @NotNull Supplier<VertexBuffer> bufferSupplier,
                                     @Nullable Consumer<ShaderInstance>  beforeShaderApply)
    {
        this.renderType = renderType;
        this.boneTransformTBO = boneTransformTBO;
        this.bufferSupplier = bufferSupplier;
        this.beforeShaderApply = beforeShaderApply;
    }

    public int getBoneTransformTextureId() {
        return boneTransformTBO.getTextureId();
    }

    @Override
    public void enter(ShaderInstance shader, VertexFormat.Mode mode, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        previousVertexArray = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        previousTexturesBinding = GL11.glGetInteger(GL31.GL_TEXTURE_BINDING_BUFFER);
        previousRasterizerDiscard = GL11.glGetBoolean(GL30.GL_RASTERIZER_DISCARD);

        if (getBoneTransformTextureId() != 0) {
            RenderSystem.activeTexture(GL13.GL_TEXTURE7);
            GL15.glBindTexture(GL31.GL_TEXTURE_BUFFER, getBoneTransformTextureId());
        }

        setupShaderState(shader, mode, beforeShaderApply, modelViewMatrix, projectionMatrix,
                Minecraft.getInstance().getWindow());
        renderType.setupRenderState();
        BufferUploader.reset();

        VertexBuffer vertexBuffer = bufferSupplier.get();
        Objects.requireNonNull(vertexBuffer);
        vertexBuffer.bind();

        if (previousRasterizerDiscard) {
            GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);
        }
    }

    @Override
    public void exit(ShaderInstance shader) {
        try {
            VertexBuffer.unbind();
            BufferUploader.reset();
            renderType.clearRenderState();

            if (getBoneTransformTextureId() != 0) {
                RenderSystem.activeTexture(GL13.GL_TEXTURE7);
                GL15.glBindTexture(GL31.GL_TEXTURE_BUFFER, 0);
            }

            GL20.glUseProgram(previousProgram);
            RenderSystem.activeTexture(previousActiveTexture);
            GL15.glBindTexture(GL31.GL_TEXTURE_BUFFER, previousTexturesBinding);
            GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
            GL30.glBindVertexArray(previousVertexArray);

            if (previousRasterizerDiscard) {
                GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
            } else {
                GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);
            }
        } finally {
            if (shader != null) shader.clear();
        }
    }

    @Override
    public boolean isGpuSkinning() {
        return true;
    }

    @Override
    public void dispatchSkinning(int numVertices) {
        // Do nothing.
    }
}
