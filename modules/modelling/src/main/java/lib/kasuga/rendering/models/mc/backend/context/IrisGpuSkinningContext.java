package lib.kasuga.rendering.models.mc.backend.context;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import lib.kasuga.mixins.client.AccessorVertexBuffer;
import lib.kasuga.rendering.models.mc.backend.transform.BoneTransformTBO;
import lib.kasuga.rendering.models.mc.backend.transform.TransformFeedbackProgram;
import lib.kasuga.rendering.models.mc.compat.iris.IrisCompat;
import lib.kasuga.rendering.models.uml.util.ModelProfiler;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IrisGpuSkinningContext implements GLContext {

    private int previousProgram;
    private int previousArrayBuffer;
    private int previousVertexArray;
    private int previousActiveTexture;
    private int previousTextureBinding;
    private boolean previousRasterizedDiscard;
    private int previousTransformFeedbackBuffer;
    private VertexBuffer currentBuffer;

    @Getter
    private final Supplier<VertexBuffer> bufferSupplier;

    @Getter
    @Nullable
    private final Consumer<ShaderInstance> beforeShaderApply;

    @Getter
    private final RenderType renderType;

    @Getter
    private final BoneTransformTBO boneTransformTBO;

    @Getter
    private final TransformFeedbackProgram program;

    @Getter
    private int overriddenPositionLocation;

    @Getter
    private final VertexFormat format;

    public IrisGpuSkinningContext(@NotNull RenderType renderType,
                                  @NotNull VertexFormat format,
                                  @NotNull Supplier<VertexBuffer> bufferSupplier,
                                  @Nullable Consumer<ShaderInstance> beforeShaderApply,
                                  BoneTransformTBO boneTransformTBO,
                                  TransformFeedbackProgram program) {
        this.renderType = renderType;
        this.boneTransformTBO = boneTransformTBO;
        this.bufferSupplier = bufferSupplier;
        this.overriddenPositionLocation = 0;
        this.beforeShaderApply = beforeShaderApply;
        this.program = program;
        this.format = format;
        currentBuffer = null;
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
        previousTextureBinding = GL11.glGetInteger(GL31.GL_TEXTURE_BINDING_BUFFER);
        previousRasterizedDiscard = GL11.glGetBoolean(GL30.GL_RASTERIZER_DISCARD);
        previousTransformFeedbackBuffer = GL11.glGetInteger(GL30.GL_TRANSFORM_FEEDBACK_BUFFER_BINDING);

        if (getBoneTransformTextureId() != 0) {
            RenderSystem.activeTexture(GL13.GL_TEXTURE7);
            GL15.glBindTexture(GL31.GL_TEXTURE_BUFFER, getBoneTransformTextureId());
        }

        renderType.setupRenderState();
        BufferUploader.reset();

        VertexBuffer irisBuffer = bufferSupplier.get();
        Objects.requireNonNull(irisBuffer);
        currentBuffer = irisBuffer;
        irisBuffer.bind();

        setupShaderState(shader, mode, beforeShaderApply, modelViewMatrix, projectionMatrix,
                Minecraft.getInstance().getWindow());
        overriddenPositionLocation = overrideIrisGpuSkinnedPositionAttribute(shader);
    }

    public void dispatchSkinning(int numVertices) {
        if (program == null) return;

        program.ensureSkinningObjects(numVertices);
        program.uploadSkinningSourceIfNeeded();
        int sourceVao = program.getSourceVaoId();
        if (sourceVao == 0 || program.getOutputBufferId() == 0) return;

        if (program.isValid()) {
            program.bind(GL13.GL_TEXTURE7, getBoneTransformTextureId());
        } else {
            return;
        }

        boolean previousRasterDiscard = GL11.glGetBoolean(GL30.GL_RASTERIZER_DISCARD);
        int previousFeedbackBuffer = GL11.glGetInteger(GL30.GL_TRANSFORM_FEEDBACK_BUFFER_BINDING);

        GL30.glBindVertexArray(sourceVao);
        GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0, program.getOutputBufferId());
        GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
        GL30.glBeginTransformFeedback(GL11.GL_POINTS);
        GL11.glDrawArrays(GL11.GL_POINTS, 0, numVertices);
        GL30.glEndTransformFeedback();
        GL30.glBindVertexArray(0);
        GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0, previousFeedbackBuffer);

        if (previousRasterDiscard) {
            GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
        } else {
            GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);
        }

        program.unbind();
    }

    @Override
    public void exit(ShaderInstance shader) {
        try {
            restoreIrisStaticAttributes(this.overriddenPositionLocation);

            VertexBuffer irisBuffer = currentBuffer;
            if (currentBuffer == null) throw new IllegalStateException("Vertex Buffer is not binding!");
            GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, ((AccessorVertexBuffer) irisBuffer).getVertexBufferId());
            format.setupBufferState();

            if (program.getOutputBufferId() != 0) {
                GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0, 0);
                GL30.glBindVertexArray(0);
            }

            VertexBuffer.unbind();
            BufferUploader.reset();
            renderType.clearRenderState();

            if (getBoneTransformTextureId() != 0) {
                RenderSystem.activeTexture(GL13.GL_TEXTURE7);
                GL15.glBindTexture(GL31.GL_TEXTURE_BUFFER, 0);
            }

            GL20.glUseProgram(previousProgram);
            GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
            GL30.glBindVertexArray(previousVertexArray);
            RenderSystem.activeTexture(previousActiveTexture);
            GL15.glBindTexture(GL31.GL_TEXTURE_BUFFER, previousTextureBinding);
            if (previousRasterizedDiscard) {
                GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
            } else {
                GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);
            }
            GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0, previousTransformFeedbackBuffer);
        } finally {
            if (shader != null) shader.clear();
        }
    }

    protected int overrideIrisGpuSkinnedPositionAttribute(ShaderInstance shader) {
        int positionLocation = getFirstAttributeLocation(shader,
                "iris_Position",
                "Position",
                "vaPosition",
                "a_Position");
        if (positionLocation < 0) {
            positionLocation = 0;
        }
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, program.getOutputBufferId());
        GL20.glEnableVertexAttribArray(positionLocation);
        GL20.glVertexAttribPointer(positionLocation, 3, GL11.GL_FLOAT, false, TransformFeedbackProgram.STRIDE, 0L);
        return positionLocation;
    }

    protected int getFirstAttributeLocation(ShaderInstance shader, String... names) {
        int programId = shader.getId();
        for (String name : names) {
            int location = GL20.glGetAttribLocation(programId, name);
            if (location >= 0) {
                return location;
            }
        }
        return -1;
    }

    protected void restoreIrisStaticAttributes(int overriddenPositionLocation) {
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, ((AccessorVertexBuffer)currentBuffer).getVertexBufferId());
        format.setupBufferState();
        if (overriddenPositionLocation > 0) {
            GL20.glDisableVertexAttribArray(overriddenPositionLocation);
        }
    }

    @Override
    public boolean isGpuSkinning() {
        return true;
    }
}
