package lib.kasuga.rendering.models.mc.backend.transform;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lib.kasuga.rendering.models.uml.dynamic.SkeletonInstance;
import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.structure.skeleton.Bone;
import lib.kasuga.rendering.models.uml.structure.skeleton.Skeleton;
import lib.kasuga.rendering.models.uml.util.ModelProfiler;
import lombok.Getter;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class BoneTransformTBO implements AutoCloseable {

    private static final int FLOATS_PER_BONE = 36;
    private static final int TEXEL_SIZE_FLOATS = 4;

    @Getter
    private final SkeletonInstance skeleton;

    private final Bone[] bones;
    private final Transform[] bindInverses;

    @Getter
    private final int boneCount;

    @Getter
    private int bufferId = 0;

    @Getter
    private int textureId = 0;

    @Getter
    private FloatBuffer uploadCache;

    @Getter
    private boolean closed = false;

    @Getter
    private long skeletonVersion;

    public BoneTransformTBO(SkeletonInstance skeletonInstance) {
        this.skeleton = skeletonInstance;
        this.bones = skeletonInstance.getSkeleton().getBones();
        this.bindInverses = new Transform[bones.length];
        this.boneCount = bones.length;
        this.skeletonVersion = skeletonInstance.getVersion();

        Skeleton skl = skeleton.getSkeleton();
        for (int i = 0; i < bones.length; i++) {
            Bone b = bones[i];
            this.bindInverses[i] = skl.getBindingInverse(b);
        }

        ensureObjects();
        ensureUploadCache();
    }

    public void uploadTransforms() {
        RenderSystem.assertOnRenderThread();
        if (closed) {
            throw new IllegalStateException("BoneTransformTBO is already closed.");
        }

        if (boneCount == 0) return;

        ensureUploadCache();
        uploadCache.clear();

        Transform identity = new Transform();
        for (int i = 0; i < bones.length; i++) {
            Transform absTransform = skeleton
                    .getAbsoluteTransforms()
                    .getOrDefault(bones[i], identity);
            putTransform(uploadCache, absTransform, bindInverses[i]);
        }

        uploadCache.flip();
        refreshGpuBuffer();
    }

    protected void refreshGpuBuffer() {
        int previousTextureBinding = GL11.glGetInteger(GL31.GL_TEXTURE_BINDING_BUFFER);
        try {
            GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, bufferId);
            GL15.glBufferData(GL31.GL_TEXTURE_BUFFER, uploadCache, GL15.GL_DYNAMIC_DRAW);
            GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, textureId);
            GL31.glTexBuffer(GL31.GL_TEXTURE_BUFFER, GL30.GL_RGBA32F, bufferId);
        } finally {
            GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, previousTextureBinding);
            GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, 0);
        }
    }

    public void bindToTextureUnit(int textureUnit) {
        RenderSystem.assertOnRenderThread();
        if (textureId == 0) return;
        GlStateManager._activeTexture(textureUnit);
        GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, textureId);
    }

    public boolean isValid() {
        return !closed && textureId != 0 && bufferId != 0;
    }

    protected static void putTransform(FloatBuffer target, Transform absTransform, Transform bindInverse) {
        Matrix4f trans = new Matrix4f();
        trans.set(absTransform.transform());
        target.put(trans.m00()).put(trans.m01()).put(trans.m02()).put(trans.m30());
        target.put(trans.m10()).put(trans.m11()).put(trans.m12()).put(trans.m31());
        target.put(trans.m20()).put(trans.m21()).put(trans.m22()).put(trans.m32());

        trans.set(bindInverse.transform());
        target.put(trans.m00()).put(trans.m01()).put(trans.m02()).put(trans.m30());
        target.put(trans.m10()).put(trans.m11()).put(trans.m12()).put(trans.m31());
        target.put(trans.m20()).put(trans.m21()).put(trans.m22()).put(trans.m32());

        Matrix3f norm = absTransform.normal();
        target.put(norm.m00()).put(norm.m01()).put(norm.m02()).put(0.0f);
        target.put(norm.m10()).put(norm.m11()).put(norm.m12()).put(0.0f);
        target.put(norm.m20()).put(norm.m21()).put(norm.m22()).put(0.0f);
    }

    public void updateForVersion() {
        if (closed) return;
        long currentVersion = skeleton.getVersion();
        if (currentVersion == skeletonVersion) return;

        long uploadStart = ModelProfiler.start();

        uploadTransforms();

        skeletonVersion = currentVersion;
        if (ModelProfiler.enabled()) {
            ModelProfiler.record(
                    "Skinning.gpu.uploadBones", uploadStart,
                    "bones=" + boneCount + ", texels=" + (boneCount * TEXEL_SIZE_FLOATS)
            );
        }
    }

    protected void ensureObjects() {
        if (bufferId == 0) {
            bufferId = GL15.glGenBuffers();
        }
        if (textureId == 0) {
            textureId = GL11.glGenTextures();
        }
    }

    protected void ensureUploadCache() {
        int requiredFloats = boneCount * FLOATS_PER_BONE;
        if (uploadCache != null && uploadCache.capacity() >= requiredFloats) {
            return;
        }
        if (uploadCache != null) {
            MemoryUtil.memFree(uploadCache);
        }
        uploadCache = MemoryUtil.memAllocFloat(requiredFloats);
    }

    @Override
    public void close() throws Exception {
        if (closed) return;
        if (bufferId != 0) {
            GL15.glDeleteBuffers(bufferId);
            bufferId = 0;
        }
        if (textureId != 0) {
            GL11.glDeleteTextures(textureId);
            textureId = 0;
        }
        if (uploadCache != null) {
            MemoryUtil.memFree(uploadCache);
            uploadCache = null;
        }
        closed = true;
    }
}
