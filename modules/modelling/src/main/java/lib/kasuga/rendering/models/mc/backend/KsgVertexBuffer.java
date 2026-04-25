package lib.kasuga.rendering.models.mc.backend;

import com.mojang.blaze3d.vertex.*;
import lib.kasuga.mixins.client.AccessorBufferBuilder;
import lib.kasuga.rendering.models.mc.backend.data_type.KasugaShaderInstance;
import lib.kasuga.rendering.models.mc.compat.iris.IrisCompat;
import lib.kasuga.rendering.models.uml.backend.VersionedBackendRenderable;
import lib.kasuga.rendering.models.uml.bridge.Bridge;
import lib.kasuga.rendering.models.uml.dynamic.ModelInstance;
import lib.kasuga.rendering.models.uml.math.BoneContext;
import lib.kasuga.rendering.models.uml.math.TangentHelper;
import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.math.binding.BoneBindingFunc;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.Mesh;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.skeleton.Bone;
import lib.kasuga.rendering.models.uml.structure.skeleton.SkeletonInstance;
import lib.kasuga.structure.Pair;
import lombok.Getter;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class KsgVertexBuffer implements AutoCloseable, VersionedBackendRenderable {

    private final ByteBuffer buffer;
    private boolean building = false;
    private final int vertexSize, numVertices;
    private final HashMap<Vertex, HashMap<Mesh, Integer[]>> vertexMap;
    private final HashMap<VertexFormatElement, Integer> offsets;
    private final HashMap<VertexFormatElement, Integer> bufOffsets;
    private VertexFormatElement uv1_element, uv2_element;
    private float minX = Float.POSITIVE_INFINITY;
    private float minY = Float.POSITIVE_INFINITY;
    private float minZ = Float.POSITIVE_INFINITY;
    private float maxX = Float.NEGATIVE_INFINITY;
    private float maxY = Float.NEGATIVE_INFINITY;
    private float maxZ = Float.NEGATIVE_INFINITY;
    private Vertex[] skinningVertices = new Vertex[0];
    private Mesh[] skinningMeshes = new Mesh[0];
    private int[] skinningIndices = new int[0];
    private Mesh[] tangentMeshes = new Mesh[0];
    private float[] basePositions = new float[0];
    private float[] baseNormals = new float[0];
    private BoneBindingFunc[] bindingFuncs = new BoneBindingFunc[0];
    private int[] boneWeightOffsets = new int[0];
    private int[] boneWeightCounts = new int[0];
    private Bone[] skinningBones = new Bone[0];
    private float[] skinningWeights = new float[0];
    private ByteBuffer uploadCache;
    private boolean uploadCacheValid = false;
    private int uploadCacheVertexSize = -1;
    private int uploadCachePackedLight = -1;
    private int uploadCachePackedOverlay = -1;
    private boolean uploadCacheReadAlpha = true;
    private float uploadCacheBrightness = Float.NaN;
    private ByteBuffer irisStaticCache;
    private boolean irisStaticCacheValid = false;
    private int irisStaticCacheVertexSize = -1;
    private int irisStaticCachePackedLight = -1;
    private int irisStaticCachePackedOverlay = -1;
    private boolean irisStaticCacheReadAlpha = true;
    private float irisStaticCacheBrightness = Float.NaN;

    @Getter
    private boolean closed = false;

    @Getter
    private final Builder modifier;
    private static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN);
    private static final int NEW_ENTITY_POSITION_OFFSET = getFormatOffset(DefaultVertexFormat.NEW_ENTITY, VertexFormatElement.POSITION);
    private static final int NEW_ENTITY_COLOR_OFFSET = getFormatOffset(DefaultVertexFormat.NEW_ENTITY, VertexFormatElement.COLOR);
    private static final int NEW_ENTITY_UV0_OFFSET = getFormatOffset(DefaultVertexFormat.NEW_ENTITY, VertexFormatElement.UV0);
    private static final int NEW_ENTITY_UV1_OFFSET = getFormatOffset(DefaultVertexFormat.NEW_ENTITY, VertexFormatElement.UV1);
    private static final int NEW_ENTITY_UV2_OFFSET = getFormatOffset(DefaultVertexFormat.NEW_ENTITY, VertexFormatElement.UV2);
    private static final int NEW_ENTITY_NORMAL_OFFSET = getFormatOffset(DefaultVertexFormat.NEW_ENTITY, VertexFormatElement.NORMAL);

    public interface ElementUploader {
        void upload(BufferBuilder builder, long pointer, int vertexIndex,
                    VertexFormatElement element, ByteBuffer buffer,
                    PoseStack.Pose pose, float brightness,
                    int packedLight, int packedOverlay,
                    boolean readAlpha);
    }

    public KsgVertexBuffer(int numVertices, int vertexSize, VertexFormat format, Builder modifier) {
        this.buffer = ByteBuffer.allocateDirect(numVertices * vertexSize);
        buffer.order(ByteOrder.nativeOrder());
        this.building = true;
        this.vertexSize = vertexSize;
        this.numVertices = numVertices;
        this.vertexMap = new HashMap<>();
        offsets = new HashMap<>();
        bufOffsets = new HashMap<>();
        int o = 0, ob = 0;
        for (VertexFormatElement element : format.getElements()) {
            offsets.put(element, o);
            bufOffsets.put(element, ob);
            if (!element.equals(VertexFormatElement.UV1) && !element.equals(VertexFormatElement.UV2)) {
                if (element.equals(VertexFormatElement.COLOR)) ob += element.byteSize() * 2;
                else ob += element.byteSize();
            }
            o += element.byteSize();
        }
        this.modifier = modifier;
        uv1_element = null; uv2_element = null;
        findUv1AndUv2(format);
    }

    @Override
    public void close() throws Exception {
        MemoryUtil.memFree(buffer);
        if (uploadCache != null) {
            MemoryUtil.memFree(uploadCache);
            uploadCache = null;
        }
        if (irisStaticCache != null) {
            MemoryUtil.memFree(irisStaticCache);
            irisStaticCache = null;
        }
        closed = true;
    }

    public void checkClosed() {
        if (closed) throw new IllegalStateException("This buffer is already closed!");
    }

    private void findUv1AndUv2(VertexFormat format) {
        for (VertexFormatElement element : format.getElements()) {
            if (!element.usage().equals(VertexFormatElement.Usage.UV)) continue;
            int index = element.index();
            if (index == 3) {
                uv1_element = element;
            } else if (index == 4) {
                uv2_element = element;
            }
        }
    }

    private long getPos(long pointer, VertexFormatElement element) {
        return pointer + offsets.get(element);
    }

    private int getBufPos(long index, VertexFormatElement element) {
        return (int) ((index * vertexSize) + bufOffsets.get(element));
    }

    public void uploadOnIrisPresent(BufferBuilder builder,
                                    PoseStack.Pose pose,
                                    float brightness,
                                    int packedLight,
                                    int packedOverlay,
                                    boolean readAlpha) {
        checkClosed();
        AccessorBufferBuilder accessor = (AccessorBufferBuilder) builder;
        ByteBufferBuilder dstBuffer = accessor.getBuffer();
        int avs = accessor.getVertexSize();
        long pointer = dstBuffer.reserve(avs * numVertices);
        if (!isIrisStaticCacheValid(avs, brightness, packedLight, packedOverlay, readAlpha)) {
            ensureIrisStaticCache(avs);
            fillIrisStaticCache(MemoryUtil.memAddress(irisStaticCache), avs, brightness, packedLight, packedOverlay, readAlpha);
            irisStaticCacheVertexSize = avs;
            irisStaticCacheBrightness = brightness;
            irisStaticCachePackedLight = packedLight;
            irisStaticCachePackedOverlay = packedOverlay;
            irisStaticCacheReadAlpha = readAlpha;
            irisStaticCacheValid = true;
        }
        long staticPointer = MemoryUtil.memAddress(irisStaticCache);
        int srcPositionOffset = bufOffsets.get(VertexFormatElement.POSITION);
        int srcNormalOffset = bufOffsets.get(VertexFormatElement.NORMAL);
        org.joml.Matrix4f poseMatrix = pose.pose();
        org.joml.Matrix3f normalMatrix = pose.normal();
        for (int i = 0; i < numVertices; i++) {
            long vertexPointer = pointer + (long) i * avs;
            long cachedVertexPointer = staticPointer + (long) i * avs;
            int vertexOffset = i * vertexSize;
            MemoryUtil.memCopy(cachedVertexPointer + NEW_ENTITY_COLOR_OFFSET, vertexPointer + NEW_ENTITY_COLOR_OFFSET, 4L);
            MemoryUtil.memCopy(cachedVertexPointer + NEW_ENTITY_UV0_OFFSET, vertexPointer + NEW_ENTITY_UV0_OFFSET, 8L);
            MemoryUtil.memCopy(cachedVertexPointer + NEW_ENTITY_UV1_OFFSET, vertexPointer + NEW_ENTITY_UV1_OFFSET, 4L);
            MemoryUtil.memCopy(cachedVertexPointer + NEW_ENTITY_UV2_OFFSET, vertexPointer + NEW_ENTITY_UV2_OFFSET, 4L);
            int bufOffset = vertexOffset + srcPositionOffset;
            float x = buffer.getFloat(bufOffset);
            float y = buffer.getFloat(bufOffset + 4);
            float z = buffer.getFloat(bufOffset + 8);
            MemoryUtil.memPutFloat(vertexPointer + NEW_ENTITY_POSITION_OFFSET,
                    poseMatrix.m00() * x + poseMatrix.m10() * y + poseMatrix.m20() * z + poseMatrix.m30());
            MemoryUtil.memPutFloat(vertexPointer + NEW_ENTITY_POSITION_OFFSET + 4L,
                    poseMatrix.m01() * x + poseMatrix.m11() * y + poseMatrix.m21() * z + poseMatrix.m31());
            MemoryUtil.memPutFloat(vertexPointer + NEW_ENTITY_POSITION_OFFSET + 8L,
                    poseMatrix.m02() * x + poseMatrix.m12() * y + poseMatrix.m22() * z + poseMatrix.m32());

            bufOffset = vertexOffset + srcNormalOffset;
            float nx = ((float) buffer.get(bufOffset)) / 127f;
            float ny = ((float) buffer.get(bufOffset + 1)) / 127f;
            float nz = ((float) buffer.get(bufOffset + 2)) / 127f;
            float tx = normalMatrix.m00() * nx + normalMatrix.m10() * ny + normalMatrix.m20() * nz;
            float ty = normalMatrix.m01() * nx + normalMatrix.m11() * ny + normalMatrix.m21() * nz;
            float tz = normalMatrix.m02() * nx + normalMatrix.m12() * ny + normalMatrix.m22() * nz;
            putNormal(vertexPointer + NEW_ENTITY_NORMAL_OFFSET, tx, ty, tz);
        }
        accessor.setVertices(accessor.getVertices() + numVertices);
    }

    private boolean isIrisStaticCacheValid(int vertexSize, float brightness, int packedLight, int packedOverlay, boolean readAlpha) {
        return irisStaticCacheValid &&
                irisStaticCache != null &&
                irisStaticCacheVertexSize == vertexSize &&
                irisStaticCachePackedLight == packedLight &&
                irisStaticCachePackedOverlay == packedOverlay &&
                irisStaticCacheReadAlpha == readAlpha &&
                Float.compare(irisStaticCacheBrightness, brightness) == 0;
    }

    private void ensureIrisStaticCache(int vertexSize) {
        int size = vertexSize * numVertices;
        if (irisStaticCache != null && irisStaticCache.capacity() >= size) {
            irisStaticCache.clear();
            return;
        }
        if (irisStaticCache != null) {
            MemoryUtil.memFree(irisStaticCache);
        }
        irisStaticCache = MemoryUtil.memAlloc(size);
        irisStaticCache.order(ByteOrder.nativeOrder());
    }

    private void invalidateIrisStaticCache() {
        irisStaticCacheValid = false;
    }

    private void fillIrisStaticCache(long pointer, int avs, float brightness, int packedLight, int packedOverlay, boolean readAlpha) {
        int srcColorOffset = bufOffsets.get(VertexFormatElement.COLOR);
        int srcUv0Offset = bufOffsets.get(VertexFormatElement.UV0);
        float colorScale = brightness / 255f;
        for (int i = 0; i < numVertices; i++) {
            long vertexPointer = pointer + (long) i * avs;
            int vertexOffset = i * vertexSize;
            int bufOffset = vertexOffset + srcColorOffset;
            int a = buffer.get(bufOffset) & 0xff;
            int b = buffer.get(bufOffset + 1) & 0xff;
            int g = buffer.get(bufOffset + 2) & 0xff;
            int r = buffer.get(bufOffset + 3) & 0xff;
            int ma = buffer.get(bufOffset + 4) & 0xff;
            int mb = buffer.get(bufOffset + 5) & 0xff;
            int mg = buffer.get(bufOffset + 6) & 0xff;
            int mr = buffer.get(bufOffset + 7) & 0xff;

            int af = readAlpha ? (a * ma) / 255 : ma;
            int bf = (int) (b * mb * colorScale);
            int gf = (int) (g * mg * colorScale);
            int rf = (int) (r * mr * colorScale);
            int colorFinal = af << 24 | bf << 16 | gf << 8 | rf;
            MemoryUtil.memPutInt(vertexPointer + NEW_ENTITY_COLOR_OFFSET, IS_LITTLE_ENDIAN ?
                    colorFinal :
                    Integer.reverseBytes(colorFinal)
            );

            bufOffset = vertexOffset + srcUv0Offset;
            MemoryUtil.memPutFloat(vertexPointer + NEW_ENTITY_UV0_OFFSET, buffer.getFloat(bufOffset));
            MemoryUtil.memPutFloat(vertexPointer + NEW_ENTITY_UV0_OFFSET + 4L, buffer.getFloat(bufOffset + 4));
            putPackedUV(vertexPointer + NEW_ENTITY_UV1_OFFSET, packedOverlay);
            putPackedUV(vertexPointer + NEW_ENTITY_UV2_OFFSET, packedLight);
        }
    }

    public void upload(BufferBuilder builder,
                       PoseStack.Pose pose,
                       @Nullable KasugaShaderInstance shader,
                       float brightness, float emissiveStrength,
                       int packedLight, int packedOverlay,
                       boolean readAlpha) {
        if (IrisCompat.isUsingShaderPack()) {
            uploadOnIrisPresent(builder, pose, brightness, packedLight, packedOverlay, readAlpha);
            return;
        }
        checkClosed();
        Objects.requireNonNull(shader);
        shader.setCurrentPose(pose);
        shader.setEmissiveStrength(emissiveStrength);
        shader.apply();
        AccessorBufferBuilder accessor = (AccessorBufferBuilder) builder;
        ByteBufferBuilder buf = accessor.getBuffer();
        int avs = accessor.getVertexSize();
        long pointer = buf.reserve(avs * numVertices);
        if (isUploadCacheValid(avs, brightness, packedLight, packedOverlay, readAlpha)) {
            MemoryUtil.memCopy(MemoryUtil.memAddress(uploadCache), pointer, (long) avs * numVertices);
            accessor.setVertices(accessor.getVertices() + numVertices);
            return;
        }
        ensureUploadCache(avs);
        long cachePointer = MemoryUtil.memAddress(uploadCache);
        fillUploadCache(cachePointer, avs, brightness, packedLight, packedOverlay, readAlpha);
        MemoryUtil.memCopy(cachePointer, pointer, (long) avs * numVertices);
        uploadCacheVertexSize = avs;
        uploadCacheBrightness = brightness;
        uploadCachePackedLight = packedLight;
        uploadCachePackedOverlay = packedOverlay;
        uploadCacheReadAlpha = readAlpha;
        uploadCacheValid = true;
        accessor.setVertices(accessor.getVertices() + numVertices);
    }

    private boolean isUploadCacheValid(int vertexSize, float brightness, int packedLight, int packedOverlay, boolean readAlpha) {
        return uploadCacheValid &&
                uploadCache != null &&
                uploadCacheVertexSize == vertexSize &&
                uploadCachePackedLight == packedLight &&
                uploadCachePackedOverlay == packedOverlay &&
                uploadCacheReadAlpha == readAlpha &&
                Float.compare(uploadCacheBrightness, brightness) == 0;
    }

    private void ensureUploadCache(int vertexSize) {
        int size = vertexSize * numVertices;
        if (uploadCache != null && uploadCache.capacity() >= size) {
            uploadCache.clear();
            return;
        }
        if (uploadCache != null) {
            MemoryUtil.memFree(uploadCache);
        }
        uploadCache = MemoryUtil.memAlloc(size);
        uploadCache.order(ByteOrder.nativeOrder());
    }

    private void invalidateUploadCache() {
        uploadCacheValid = false;
        invalidateIrisStaticCache();
    }

    private void fillUploadCache(long pointer, int avs, float brightness, int packedLight, int packedOverlay, boolean readAlpha) {
        int dstPositionOffset = offsets.get(VertexFormatElement.POSITION);
        int dstColorOffset = offsets.get(VertexFormatElement.COLOR);
        int dstUv0Offset = offsets.get(VertexFormatElement.UV0);
        int dstTangentOffset = offsets.get(RenderState.TANGENT);
        int dstNormalOffset = offsets.get(VertexFormatElement.NORMAL);
        int dstUv1LightOffset = offsets.get(VertexFormatElement.UV1);
        int dstUv2LightOffset = offsets.get(VertexFormatElement.UV2);
        int srcPositionOffset = bufOffsets.get(VertexFormatElement.POSITION);
        int srcColorOffset = bufOffsets.get(VertexFormatElement.COLOR);
        int srcUv0Offset = bufOffsets.get(VertexFormatElement.UV0);
        int srcTangentOffset = bufOffsets.get(RenderState.TANGENT);
        int srcNormalOffset = bufOffsets.get(VertexFormatElement.NORMAL);
        int srcUv1Offset = uv1_element == null ? -1 : bufOffsets.get(uv1_element);
        int srcUv2Offset = uv2_element == null ? -1 : bufOffsets.get(uv2_element);
        int dstUv1Offset = uv1_element == null ? -1 : offsets.get(uv1_element);
        int dstUv2Offset = uv2_element == null ? -1 : offsets.get(uv2_element);
        long bufferPointer = MemoryUtil.memAddress(buffer);
        float colorScale = brightness / 255f;
        for (int i = 0; i < numVertices; i++) {
            long vertexPointer = pointer + (long) i * avs;
            int vertexOffset = i * vertexSize;
            long sourcePointer = bufferPointer + vertexOffset;
            MemoryUtil.memCopy(sourcePointer + srcPositionOffset, vertexPointer + dstPositionOffset, 12L);

            long offset = vertexPointer + dstColorOffset;
            int bufOffset = vertexOffset + srcColorOffset;

            int a = buffer.get(bufOffset) & 0xff;
            int b = buffer.get(bufOffset + 1) & 0xff;
            int g = buffer.get(bufOffset + 2) & 0xff;
            int r = buffer.get(bufOffset + 3) & 0xff;
            int ma = buffer.get(bufOffset + 4) & 0xff;
            int mb = buffer.get(bufOffset + 5) & 0xff;
            int mg = buffer.get(bufOffset + 6) & 0xff;
            int mr = buffer.get(bufOffset + 7) & 0xff;

            int af = readAlpha ? (a * ma) / 255 : ma;
            int bf = (int) (b * mb * colorScale);
            int gf = (int) (g * mg * colorScale);
            int rf = (int) (r * mr * colorScale);
            int colorFinal = af << 24 | bf << 16 | gf << 8 | rf;
            MemoryUtil.memPutInt(offset, IS_LITTLE_ENDIAN ?
                    colorFinal :
                    Integer.reverseBytes(colorFinal)
            );

            MemoryUtil.memCopy(sourcePointer + srcUv0Offset, vertexPointer + dstUv0Offset, 8L);
            MemoryUtil.memCopy(sourcePointer + srcTangentOffset, vertexPointer + dstTangentOffset, 16L);
            MemoryUtil.memCopy(sourcePointer + srcNormalOffset, vertexPointer + dstNormalOffset, 3L);

            offset = vertexPointer + dstUv1LightOffset;
            putPackedUV(offset, packedOverlay);

            offset = vertexPointer + dstUv2LightOffset;
            putPackedUV(offset, packedLight);

            if (uv1_element != null) {
                MemoryUtil.memCopy(sourcePointer + srcUv1Offset, vertexPointer + dstUv1Offset, 8L);
            }
            if (uv2_element != null) {
                MemoryUtil.memCopy(sourcePointer + srcUv2Offset, vertexPointer + dstUv2Offset, 8L);
            }
        }
    }

    private static void putPackedUV(long pointer, int packedUv) {
        if (IS_LITTLE_ENDIAN) {
            MemoryUtil.memPutInt(pointer, packedUv);
        } else {
            MemoryUtil.memPutShort(pointer, (short)(packedUv & '\uffff'));
            MemoryUtil.memPutShort(pointer + 2L, (short)(packedUv >> 16 & '\uffff'));
        }
    }

    private static void putNormal(long pointer, float x, float y, float z) {
        MemoryUtil.memPutByte(pointer, (byte) ((int) (x * 127) & 0xFF));
        MemoryUtil.memPutByte(pointer + 1L, (byte) ((int) (y * 127) & 0xFF));
        MemoryUtil.memPutByte(pointer + 2L, (byte) ((int) (z * 127) & 0xFF));
        MemoryUtil.memPutByte(pointer + 3L, (byte) 0);
    }

    private static int getFormatOffset(VertexFormat format, VertexFormatElement element) {
        int offset = 0;
        for (VertexFormatElement current : format.getElements()) {
            if (current.equals(element)) {
                return offset;
            }
            offset += current.byteSize();
        }
        return -1;
    }

    public void ensureBuilding() {
        if (!building) throw new IllegalStateException("Not building vertex buffer");
    }

    public void addVertex(ByteBuffer vertexData, int offset) {
        ensureBuilding();
        checkClosed();
        if (vertexData.capacity() != vertexSize) {
            throw new IllegalArgumentException("Vertex data length does not match vertex size");
        }
        ByteBuffer src = vertexData.duplicate();
        src.clear();
        src.limit(vertexSize);
        ByteBuffer dst = buffer.duplicate();
        dst.position(offset * this.vertexSize);
        dst.put(src);
        vertexData.clear();
    }

    private void includeBounds(float x, float y, float z) {
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        minZ = Math.min(minZ, z);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
        maxZ = Math.max(maxZ, z);
    }

    private void includeBounds(Bounds bounds) {
        if (bounds == null || !bounds.hasBounds()) return;
        includeBounds(bounds.minX, bounds.minY, bounds.minZ);
        includeBounds(bounds.maxX, bounds.maxY, bounds.maxZ);
    }

    public boolean hasBounds() {
        return minX <= maxX && minY <= maxY && minZ <= maxZ;
    }

    public AABB getBounds(@Nullable Vector3f position) {
        double x = position == null ? 0.0 : position.x();
        double y = position == null ? 0.0 : position.y();
        double z = position == null ? 0.0 : position.z();
        return new AABB(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
    }

    private void resetBounds() {
        minX = Float.POSITIVE_INFINITY;
        minY = Float.POSITIVE_INFINITY;
        minZ = Float.POSITIVE_INFINITY;
        maxX = Float.NEGATIVE_INFINITY;
        maxY = Float.NEGATIVE_INFINITY;
        maxZ = Float.NEGATIVE_INFINITY;
    }

    private void setSkinningData(ArrayList<Vertex> vertices, ArrayList<Mesh> meshes, ArrayList<Integer> indices, Mesh[] tangentMeshes) {
        this.skinningVertices = vertices.toArray(new Vertex[0]);
        this.skinningMeshes = meshes.toArray(new Mesh[0]);
        this.skinningIndices = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            this.skinningIndices[i] = indices.get(i);
        }
        this.tangentMeshes = tangentMeshes;
        this.basePositions = new float[vertices.size() * 3];
        this.baseNormals = new float[vertices.size() * 3];
        this.bindingFuncs = new BoneBindingFunc[vertices.size()];
        this.boneWeightOffsets = new int[vertices.size()];
        this.boneWeightCounts = new int[vertices.size()];
        ArrayList<Bone> bones = new ArrayList<>();
        ArrayList<Float> weights = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            Mesh mesh = meshes.get(i);
            int componentOffset = i * 3;
            Vector3f position = vertex.getPosition();
            Vector3f normal = vertex.getNormal(mesh);
            basePositions[componentOffset] = position.x();
            basePositions[componentOffset + 1] = position.y();
            basePositions[componentOffset + 2] = position.z();
            baseNormals[componentOffset] = normal.x();
            baseNormals[componentOffset + 1] = normal.y();
            baseNormals[componentOffset + 2] = normal.z();
            bindingFuncs[i] = vertex.getBinding().getFunc();
            boneWeightOffsets[i] = bones.size();
            Pair<Bone, Float>[] bindingWeights = vertex.getBinding().getWeights();
            boneWeightCounts[i] = bindingWeights.length;
            for (Pair<Bone, Float> pair : bindingWeights) {
                bones.add(pair.getFirst());
                weights.add(pair.getSecond());
            }
        }
        this.skinningBones = bones.toArray(new Bone[0]);
        this.skinningWeights = new float[weights.size()];
        for (int i = 0; i < weights.size(); i++) {
            this.skinningWeights[i] = weights.get(i);
        }
    }

    @Override
    public void updateForVersion(ModelInstance modelInstance, Bridge<?> bridge) {
        checkClosed();
        invalidateUploadCache();
        SkeletonInstance skeleton = modelInstance.getSkeletonInstance();
        int vertexCount = skinningVertices.length;
        resetBounds();
        Bounds bounds = new Bounds();
        updateSkinningRange(modelInstance, bridge, skeleton, 0, vertexCount, bounds);
        includeBounds(bounds);
        for (Mesh mesh : tangentMeshes) {
            modifier.calculateTangent(mesh);
        }
    }

    private void updateSkinningRange(ModelInstance modelInstance, Bridge<?> bridge, SkeletonInstance skeleton,
                                     int startInclusive, int endExclusive, Bounds bounds) {
        List<BoneContext> contexts = new ArrayList<>();
        Vector3f position = new Vector3f();
        Vector3f normal = new Vector3f();
        Vector3f scratchPosition = new Vector3f();
        Vector3f scratchNormal = new Vector3f();
        for (int i = startInclusive; i < endExclusive; i++) {
            Vertex vertex = skinningVertices[i];
            Mesh mesh = skinningMeshes[i];
            BoneBindingFunc func = bindingFuncs[i];
            if (func == null || func == BoneBindingFunc.IDENTITY) {
                setBasePosition(i, position);
                setBaseNormal(i, normal);
            } else if (func == BoneBindingFunc.BDEF) {
                applyBdef(i, skeleton, position, normal, scratchPosition, scratchNormal);
            } else {
                skeleton.collectBoneContexts(contexts, vertex);
                Vertex transformed = func.apply(vertex, contexts);
                position.set(transformed.getPosition());
                normal.set(transformed.getNormal(mesh));
            }
            putSkinnedVertex(skinningIndices[i], position, normal);
            bounds.include(position);
        }
    }

    private void setBasePosition(int index, Vector3f position) {
        int componentOffset = index * 3;
        position.set(basePositions[componentOffset], basePositions[componentOffset + 1], basePositions[componentOffset + 2]);
    }

    private void setBaseNormal(int index, Vector3f normal) {
        int componentOffset = index * 3;
        normal.set(baseNormals[componentOffset], baseNormals[componentOffset + 1], baseNormals[componentOffset + 2]);
    }

    private void applyBdef(int skinningIndex, SkeletonInstance skeleton, Vector3f position, Vector3f normal,
                           Vector3f scratchPosition, Vector3f scratchNormal) {
        position.zero();
        normal.zero();
        int weightOffset = boneWeightOffsets[skinningIndex];
        int weightCount = boneWeightCounts[skinningIndex];
        if (weightCount == 0) {
            setBasePosition(skinningIndex, position);
            setBaseNormal(skinningIndex, normal);
            return;
        }
        for (int i = 0; i < weightCount; i++) {
            int index = weightOffset + i;
            Bone bone = skinningBones[index];
            float weight = skinningWeights[index];
            Transform absTransform = skeleton.getAbsoluteTransforms().get(bone);
            Pair<Transform, Transform> bindTransforms = skeleton.getSkeleton().getBoneTransforms().get(bone);
            if (absTransform == null || bindTransforms == null) continue;
            setBasePosition(skinningIndex, scratchPosition);
            bindTransforms.getSecond().apply(scratchPosition);
            absTransform.apply(scratchPosition);
            position.add(scratchPosition.mul(weight));

            setBaseNormal(skinningIndex, scratchNormal);
            absTransform.normal().transform(scratchNormal);
            normal.add(scratchNormal.mul(weight));
        }
        if (normal.lengthSquared() > 0f) {
            normal.normalize();
        }
    }

    private void putSkinnedVertex(int index, Vector3f position, Vector3f normal) {
        int posOffset = getBufPos(index, VertexFormatElement.POSITION);
        buffer.putFloat(posOffset, position.x());
        buffer.putFloat(posOffset + 4, position.y());
        buffer.putFloat(posOffset + 8, position.z());

        int normalOffset = getBufPos(index, VertexFormatElement.NORMAL);
        if (normal.lengthSquared() > 0f) {
            normal.normalize();
        }
        buffer.put(normalOffset, (byte) ((int) (normal.x() * 127) & 0xFF));
        buffer.put(normalOffset + 1, (byte) ((int) (normal.y() * 127) & 0xFF));
        buffer.put(normalOffset + 2, (byte) ((int) (normal.z() * 127) & 0xFF));
    }

    private static class Bounds {
        private float minX = Float.POSITIVE_INFINITY;
        private float minY = Float.POSITIVE_INFINITY;
        private float minZ = Float.POSITIVE_INFINITY;
        private float maxX = Float.NEGATIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;
        private float maxZ = Float.NEGATIVE_INFINITY;

        private void include(Vector3f position) {
            minX = Math.min(minX, position.x());
            minY = Math.min(minY, position.y());
            minZ = Math.min(minZ, position.z());
            maxX = Math.max(maxX, position.x());
            maxY = Math.max(maxY, position.y());
            maxZ = Math.max(maxZ, position.z());
        }

        private boolean hasBounds() {
            return minX <= maxX && minY <= maxY && minZ <= maxZ;
        }
    }

    public void frozen(HashMap<Vertex, HashMap<Mesh, Integer[]>> boneTransformMap) {
        ensureBuilding();
        checkClosed();
        this.vertexMap.putAll(boneTransformMap);
        this.building = false;
    }

    public static class Builder implements VertexConsumer {

        private final KsgVertexBuffer vertexBuffer;
        private final VertexFormat format;
        private final int vertexDataSize;
        private final HashMap<Vertex, HashMap<Mesh, ArrayList<Integer>>> boneVertexMap;
        private final ArrayList<Vertex> vertices;
        private final ArrayList<Vertex> skinningVertices;
        private final ArrayList<Mesh> skinningMeshes;
        private final ArrayList<Integer> skinningIndices;
        private final VertexFormatElement uv1Element;
        private final VertexFormatElement uv2Element;
        private final int positionOffset;
        private final int colorOffset;
        private final int meshColorOffset;
        private final int normalOffset;
        private final int uv0Offset;
        private final int uv1Offset;
        private final int uv2Offset;
        private final int tangentOffset;

        private int vertexIndex = 0;
        private int indexVertexInMesh;
        private Vector3f position;
        private int color;
        private Vector3f normal;
        private Vector2f uv0;
        private Vector2f uv1;
        private Vector2f uv2;
        private Vector4f tangent;
        private boolean modifying;
        private Pair<Mesh, Integer[]> modifyingVertexIndices;
        private int buildingIndex;

        public Builder(Model model,
                       VertexFormat format) {
            this.format = format;
            this.vertexDataSize = format.getVertexSize() - shouldIgnoredBytes(format) + 4;
            this.uv1Element = findUvElement(format, 1);
            this.uv2Element = findUvElement(format, 2);
            this.positionOffset = getOffsetFor(VertexFormatElement.POSITION);
            this.colorOffset = getOffsetFor(VertexFormatElement.COLOR);
            this.meshColorOffset = colorOffset + 4;
            this.normalOffset = getOffsetFor(VertexFormatElement.NORMAL);
            this.uv0Offset = getOffsetFor(VertexFormatElement.UV0);
            this.uv1Offset = uv1Element == null ? -1 : getOffsetFor(uv1Element);
            this.uv2Offset = uv2Element == null ? -1 : getOffsetFor(uv2Element);
            this.tangentOffset = getOffsetFor(RenderState.TANGENT);
            this.vertexBuffer = new KsgVertexBuffer(
                    4 * model.getMeshes().length,
                    vertexDataSize, format, this
            );
            position = new Vector3f();
            normal = new Vector3f();
            uv0 = new Vector2f();
            uv1 = new Vector2f();
            uv2 = new Vector2f();
            tangent = new Vector4f();
            reset();
            indexVertexInMesh = 0;
            boneVertexMap = new HashMap<>();
            vertices = new ArrayList<>(4 * model.getMeshes().length);
            skinningVertices = new ArrayList<>(4 * model.getMeshes().length);
            skinningMeshes = new ArrayList<>(4 * model.getMeshes().length);
            skinningIndices = new ArrayList<>(4 * model.getMeshes().length);
            this.modifying = false;
            this.modifyingVertexIndices = null;
            this.buildingIndex = 0;
        }

        private static VertexFormatElement findUvElement(VertexFormat format, int uvIndex) {
            for (VertexFormatElement element : format.getElements()) {
                if (!element.usage().equals(VertexFormatElement.Usage.UV)) continue;
                if (element.index() == uvIndex + 2) return element;
            }
            return null;
        }

        private int shouldIgnoredBytes(VertexFormat format) {
            int ignoredBytes = 0;
            for (VertexFormatElement element : format.getElements()) {
                if (element.equals(VertexFormatElement.UV1) ||
                        element.equals(VertexFormatElement.UV2)) {
                    ignoredBytes += element.byteSize();
                }
            }
            return ignoredBytes;
        }

        private void reset() {
            position.set(0f, 0f, 0f);
            color = 0xFFFFFFFF;
            normal.set(0f, 0f, 0f);
            uv0.set(0f, 0f);
            uv1.set(0f, 0f);
            uv2.set(0f, 0f);
            tangent.set(0f, 0f, 0f, 0f);
        }

        public int getOffsetFor(VertexFormatElement element) {
            List<VertexFormatElement> elements = format.getElements();
            int index = elements.indexOf(element);
            if (index == -1) return -1;
            int offset = 0;
            for (int i = 0; i < index; i++) {
                VertexFormatElement e = elements.get(i);
                if (e.equals(VertexFormatElement.UV1) ||
                        e.equals(VertexFormatElement.UV2)) continue;
                if (e.equals(VertexFormatElement.COLOR)) offset += e.byteSize() * 2;
                else offset += e.byteSize();
            }
            return offset;
        }

        public void ensureBuilding() {
            if (!vertexBuffer.building) throw new IllegalStateException("Not building vertex buffer");
        }

        public void ensureFrozen() {
            if (!modifying) throw new IllegalStateException("Vertex buffer is still building");
        }

        public VertexFormatElement ensureHasUv(int i) {
            if (i < 1) return VertexFormatElement.UV0;
            VertexFormatElement element = switch (i) {
                case 1 -> uv1Element;
                case 2 -> uv2Element;
                default -> null;
            };
            if (element != null) {
                return element;
            }
            throw new IllegalStateException("Vertex format does not have UV" + i);
        }

        public Builder modifyVertex(Vertex vertex, Mesh mesh, float x, float y, float z) {
            ensureFrozen();
            this.position.set(x, y, z);
            HashMap<Mesh, Integer[]> map = vertexBuffer.vertexMap.get(vertex);
            if (map ==null) {
                modifyingVertexIndices = null;
                return this;
            }
            Integer[] indices = map.get(mesh);
            if (indices == null) {
                modifyingVertexIndices = null;
                return this;
            }
            modifyingVertexIndices = Pair.of(mesh, indices);
            return this;
        }

        @Override
        public Builder addVertex(float x, float y, float z) {
            this.ensureBuilding();
            if (indexVertexInMesh > 4) {
                throw new IllegalStateException("Expected quad export after fourth vertex");
            }
            if (vertexIndex > vertexBuffer.numVertices) {
                throw new IllegalStateException("Vertex index exceeds vertex buffer capacity");
            }
            this.position.set(x, y, z);
            return this;
        }

        @Override
        public Builder setColor(int r, int g, int b, int a) {
            this.color = FastColor.ABGR32.color(a, b, g, r);
            return this;
        }

        public Builder setColor(Vector4f color) {
            this.color = FastColor.ABGR32.color(
                    (int) (color.w * 255),
                    (int) (color.z * 255),
                    (int) (color.y * 255),
                    (int) (color.x * 255));
            return this;
        }

        @Override
        public @NotNull Builder setUv(float v, float v1) {
            this.uv0.set(v, v1);
            return this;
        }

        public @NotNull Builder setUv(Vector2f uv) {
            this.uv0.set(uv);
            return this;
        }

        public @NotNull Builder setUv(int uvIndex, Vector2f uv) {
            if (uvIndex < 1) return setUv(uv);
            ensureHasUv(uvIndex);
            switch (uvIndex) {
                case 1 -> this.uv1.set(uv);
                case 2 -> this.uv2.set(uv);
                default -> throw new IllegalArgumentException("Invalid UV index: " + uvIndex);
            }
            return this;
        }

        public @NotNull Builder setUv1(Vector2f uv) {
            return setUv(1, uv);
        }

        public @NotNull Builder setUv2(Vector2f uv) {
            return setUv(2, uv);
        }

        @Override
        @Deprecated
        public @NotNull Builder setUv1(int i, int i1) {
            return this;
        }

        @Override
        @Deprecated
        public @NotNull Builder setUv2(int i, int i1) {
            return this;
        }

        @Override
        public @NotNull Builder setNormal(float x, float y, float z) {
            this.normal.set(x, y, z);
            return this;
        }

        public @NotNull Builder setNormal(Vector3f normal) {
            return setNormal(normal.x(), normal.y(), normal.z());
        }

        public void ensureModifying() {
            if (modifying && modifyingVertexIndices == null) {
                throw new IllegalStateException("No vertex is being modified");
            }
        }

        public Builder update(Vector4f meshColor, Mesh mesh) {
            ensureModifying();
            for (Integer i : modifyingVertexIndices.getSecond()) {
                int j = (int) i;
                pack(j, vertices.get(j), mesh, meshColor);
            }
            return this;
        }

        public Builder pack(Vertex vertex, Mesh mesh, Vector4f meshColor) {
            ensureBuilding();
            return pack(0, vertex, mesh, meshColor);
        }

        public Builder pack(int index, Vertex vertex, Mesh mesh, Vector4f meshColor) {
            try (MemoryStack memory = MemoryStack.stackPush()) {
                if (!modifying) index = vertexIndex;
                ByteBuffer byteBuffer = memory.malloc(vertexDataSize);
                byteBuffer.putFloat(positionOffset, position.x());
                byteBuffer.putFloat(positionOffset + 4, position.y());
                byteBuffer.putFloat(positionOffset + 8, position.z());

                byteBuffer.putInt(colorOffset, color);
                int meshColorInt = FastColor.ABGR32.color(
                        (int) (meshColor.w * 255),
                        (int) (meshColor.z * 255),
                        (int) (meshColor.y * 255),
                        (int) (meshColor.x * 255)
                );
                byteBuffer.putInt(meshColorOffset, meshColorInt);

                byteBuffer.put(normalOffset, (byte) ((int) (normal.x() * 127) & 0xFF));
                byteBuffer.put(normalOffset + 1, (byte) ((int) (normal.y() * 127) & 0xFF));
                byteBuffer.put(normalOffset + 2, (byte) ((int) (normal.z() * 127) & 0xFF));

                byteBuffer.putFloat(uv0Offset, uv0.x());
                byteBuffer.putFloat(uv0Offset + 4, uv0.y());

                if (uv1Offset >= 0) {
                    byteBuffer.putFloat(uv1Offset, uv1.x());
                    byteBuffer.putFloat(uv1Offset + 4, uv1.y());
                }

                if (uv2Offset >= 0) {
                    byteBuffer.putFloat(uv2Offset, uv2.x());
                    byteBuffer.putFloat(uv2Offset + 4, uv2.y());
                }

                if (tangentOffset >= 0) {
                    byteBuffer.putFloat(tangentOffset, tangent.x());
                    byteBuffer.putFloat(tangentOffset + 4, tangent.y());
                    byteBuffer.putFloat(tangentOffset + 8, tangent.z());
                    byteBuffer.putFloat(tangentOffset + 12, tangent.w());
                }

                vertexBuffer.invalidateUploadCache();
                vertexBuffer.includeBounds(position.x(), position.y(), position.z());
                vertexBuffer.addVertex(byteBuffer, index);
                if (!modifying) {
                    boneVertexMap.computeIfAbsent(vertex, k -> new HashMap<>()).computeIfAbsent(mesh, m -> new ArrayList<>()).add(index);
                    vertices.add(vertex);
                    recordSkinningVertex(index, vertex, mesh);
                    indexVertexInMesh ++;
                    vertexIndex++;
                    buildingIndex += vertexDataSize;
                }
            }
            this.reset();
            return this;
        }

        private void recordSkinningVertex(int index, Vertex vertex, Mesh mesh) {
            skinningIndices.add(index);
            skinningVertices.add(vertex);
            skinningMeshes.add(mesh);
        }

        public Builder endMesh(Mesh mesh) {
            if (indexVertexInMesh < 1) return this;
            if (indexVertexInMesh == 1) {
                throw new IllegalStateException("Mesh ended with only 1 vertex");
            }
            if (indexVertexInMesh < 4) {
                ByteBuffer built = vertexBuffer.buffer;
                if (indexVertexInMesh < 3) {
                    ByteBuffer firstVertex = built.slice(buildingIndex - 2 * vertexDataSize, vertexDataSize);
                    ByteBuffer secondVertex = built.slice(buildingIndex - vertexDataSize, vertexDataSize);
                    Vertex vertex1 = vertices.get(vertices.size() - 2);
                    Vertex vertex2 = vertices.getLast();
                    for (int i = 0; i < vertexDataSize; i++) {
                        built.put(buildingIndex + i, firstVertex.get(i));
                    }
                    buildingIndex += vertexDataSize;
                    for (int i = 0; i < vertexDataSize; i++) {
                        built.put(buildingIndex + i, secondVertex.get(i));
                    }
                    vertices.add(vertex1);
                    vertices.add(vertex2);
                    int vertexIndex1 = vertexIndex++;
                    int vertexIndex2 = vertexIndex++;
                    boneVertexMap.computeIfAbsent(vertex1, k -> new HashMap<>()).computeIfAbsent(mesh, m -> new ArrayList<>()).add(vertexIndex1);
                    boneVertexMap.computeIfAbsent(vertex2, k -> new HashMap<>()).computeIfAbsent(mesh, m -> new ArrayList<>()).add(vertexIndex2);
                    recordSkinningVertex(vertexIndex1, vertex1, mesh);
                    recordSkinningVertex(vertexIndex2, vertex2, mesh);
                    buildingIndex += vertexDataSize;
                } else {
                    ByteBuffer thirdVertex = built.slice(buildingIndex - vertexDataSize, vertexDataSize);
                    Vertex vertex3 = vertices.getLast();
                    for (int i = 0; i < vertexDataSize; i++) {
                        built.put(buildingIndex + i, thirdVertex.get(i));
                    }
                    vertices.add(vertex3);
                    int vertexIndex3 = vertexIndex++;
                    boneVertexMap.computeIfAbsent(vertex3, k -> new HashMap<>()).computeIfAbsent(mesh, m -> new ArrayList<>()).add(vertexIndex3);
                    recordSkinningVertex(vertexIndex3, vertex3, mesh);
                    buildingIndex += vertexDataSize;
                }
            }
            indexVertexInMesh = 0;
            return this;
        }

        public Builder calculateTangent(Mesh mesh) {
            TangentHelper.computeTangents(
                    mesh,
                    v -> this.getPosUVNormal(v, mesh),
                    (v, t) -> this.setVertexTangent(v, mesh, t)
            );
            return this;
        }

        @Nullable
        public TangentHelper.PosUVNormal getPosUVNormal(Vertex vertex, Mesh mesh) {
            HashMap<Mesh, Integer[]> map = vertexBuffer.vertexMap.get(vertex);
            if (map == null) return null;
            final Integer[] pointers = map.get(mesh);
            if (pointers == null || pointers.length == 0) {
                return null;
            }
            int index = pointers[0];
            int posOffset = vertexBuffer.getBufPos(index, VertexFormatElement.POSITION);
            Vector3f position = new Vector3f(
                    vertexBuffer.buffer.getFloat(posOffset),
                    vertexBuffer.buffer.getFloat(posOffset + 4),
                    vertexBuffer.buffer.getFloat(posOffset + 8)
            );
            int uvOffset = vertexBuffer.getBufPos(index, VertexFormatElement.UV0);
            Vector2f uv = new Vector2f(
                    vertexBuffer.buffer.getFloat(uvOffset),
                    vertexBuffer.buffer.getFloat(uvOffset + 4)
            );
            int normalOffset = vertexBuffer.getBufPos(index, VertexFormatElement.NORMAL);
            float nx = ((float) vertexBuffer.buffer.get(normalOffset)) / 127f;
            float ny = ((float) vertexBuffer.buffer.get(normalOffset + 1)) / 127f;
            float nz = ((float) vertexBuffer.buffer.get(normalOffset + 2)) / 127f;
            Vector3f norm = new Vector3f(nx, ny, nz);
            norm.normalize();
            return new TangentHelper.PosUVNormal(position, uv, norm);
        }

        public void setVertexTangent(Vertex vertex, Mesh mesh, Vector4f tangent) {
            HashMap<Mesh, Integer[]> map = vertexBuffer.vertexMap.get(vertex);
            if (map == null) {
                return;
            }
            final Integer[] pointers = map.get(mesh);
            if (pointers == null || pointers.length == 0) {
                return;
            }
            for (Integer i : pointers) {
                int index = i;
                int tangentOffset = vertexBuffer.getBufPos(index, RenderState.TANGENT);
                vertexBuffer.buffer.putFloat(tangentOffset, tangent.x());
                vertexBuffer.buffer.putFloat(tangentOffset + 4, tangent.y());
                vertexBuffer.buffer.putFloat(tangentOffset + 8, tangent.z());
                vertexBuffer.buffer.putFloat(tangentOffset + 12, tangent.w());
            }
        }

        public KsgVertexBuffer build(Model model) {
            HashMap<Vertex, HashMap<Mesh, Integer[]>> boneTransformMap = new HashMap<>();
            for (Vertex bone : boneVertexMap.keySet()) {
                HashMap<Mesh, ArrayList<Integer>> map = boneVertexMap.get(bone);
                HashMap<Mesh, Integer[]> intMap = new HashMap<>();
                for (Mesh mesh : map.keySet()) {
                    ArrayList<Integer> vertexIndices = map.get(mesh);
                    intMap.put(mesh, vertexIndices.toArray(new Integer[0]));
                }
                boneTransformMap.put(bone, intMap);
            }
            vertexBuffer.frozen(boneTransformMap);
            vertexBuffer.setSkinningData(skinningVertices, skinningMeshes, skinningIndices, model.getMeshes());
            for (Mesh mesh : model.getMeshes()) {
                calculateTangent(mesh);
            }
            this.vertices.clear();
            this.modifying = true;
            return vertexBuffer;
        }
    }
}
