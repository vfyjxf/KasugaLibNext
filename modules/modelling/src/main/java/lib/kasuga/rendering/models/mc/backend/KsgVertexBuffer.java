package lib.kasuga.rendering.models.mc.backend;

import com.mojang.blaze3d.vertex.*;
import lib.kasuga.mixins.client.AccessorBufferBuilder;
import lib.kasuga.rendering.models.mc.backend.data_type.KasugaShaderInstance;
import lib.kasuga.rendering.models.mc.compat.iris.IrisCompat;
import lib.kasuga.rendering.models.uml.math.TangentHelper;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.Mesh;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
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

public class KsgVertexBuffer implements AutoCloseable {

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

    @Getter
    private boolean closed = false;

    @Getter
    private final Builder modifier;
    private static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN);

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
        int srcPositionOffset = bufOffsets.get(VertexFormatElement.POSITION);
        int srcColorOffset = bufOffsets.get(VertexFormatElement.COLOR);
        int srcUv0Offset = bufOffsets.get(VertexFormatElement.UV0);
        int srcNormalOffset = bufOffsets.get(VertexFormatElement.NORMAL);
        float colorScale = brightness / 255f;
        Vector3f pos = new Vector3f();
        Vector3f norm = new Vector3f();
        for (int i = 0; i < numVertices; i++) {
            int vertexOffset = i * vertexSize;
            int bufOffset = vertexOffset + srcPositionOffset;
            pos.set(buffer.getFloat(bufOffset), buffer.getFloat(bufOffset + 4), buffer.getFloat(bufOffset + 8));
            pose.pose().transformPosition(pos);

            bufOffset = vertexOffset + srcColorOffset;
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

            bufOffset = vertexOffset + srcUv0Offset;
            float u0 = buffer.getFloat(bufOffset);
            float v0 = buffer.getFloat(bufOffset + 4);

            bufOffset = vertexOffset + srcNormalOffset;
            float nx = ((float) buffer.get(bufOffset)) / 127f;
            float ny = ((float) buffer.get(bufOffset + 1)) / 127f;
            float nz = ((float) buffer.get(bufOffset + 2)) / 127f;
            norm.set(nx, ny, nz);
            pose.normal().transform(norm);
            nx = norm.x(); ny = norm.y(); nz = norm.z();
            builder.addVertex(pos.x(), pos.y(), pos.z(),
                    colorFinal, u0, v0, packedOverlay, packedLight, nx, ny, nz);
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
        accessor.setVertices(accessor.getVertices() + numVertices);
    }

    private static void putPackedUV(long pointer, int packedUv) {
        if (IS_LITTLE_ENDIAN) {
            MemoryUtil.memPutInt(pointer, packedUv);
        } else {
            MemoryUtil.memPutShort(pointer, (short)(packedUv & '\uffff'));
            MemoryUtil.memPutShort(pointer + 2L, (short)(packedUv >> 16 & '\uffff'));
        }
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

    public boolean hasBounds() {
        return minX <= maxX && minY <= maxY && minZ <= maxZ;
    }

    public AABB getBounds(@Nullable Vector3f position) {
        double x = position == null ? 0.0 : position.x();
        double y = position == null ? 0.0 : position.y();
        double z = position == null ? 0.0 : position.z();
        return new AABB(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
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

                vertexBuffer.includeBounds(position.x(), position.y(), position.z());
                vertexBuffer.addVertex(byteBuffer, index);
                boneVertexMap.computeIfAbsent(vertex, k -> new HashMap<>()).computeIfAbsent(mesh, m -> new ArrayList<>()).add(vertexIndex);
                vertices.add(vertex);
                indexVertexInMesh ++;
                if (!modifying) vertexIndex++;
                if (!modifying) buildingIndex += vertexDataSize;
            }
            this.reset();
            return this;
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
                    boneVertexMap.computeIfAbsent(vertex1, k -> new HashMap<>()).computeIfAbsent(mesh, m -> new ArrayList<>()).add(vertexIndex++);
                    boneVertexMap.computeIfAbsent(vertex2, k -> new HashMap<>()).computeIfAbsent(mesh, m -> new ArrayList<>()).add(vertexIndex++);
                    buildingIndex += vertexDataSize;
                } else {
                    ByteBuffer thirdVertex = built.slice(buildingIndex - vertexDataSize, vertexDataSize);
                    Vertex vertex3 = vertices.getLast();
                    for (int i = 0; i < vertexDataSize; i++) {
                        built.put(buildingIndex + i, thirdVertex.get(i));
                    }
                    vertices.add(vertex3);
                    boneVertexMap.computeIfAbsent(vertex3, k -> new HashMap<>()).computeIfAbsent(mesh, m -> new ArrayList<>()).add(vertexIndex++);
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
            for (Mesh mesh : model.getMeshes()) {
                calculateTangent(mesh);
            }
            this.vertices.clear();
            this.modifying = true;
            return vertexBuffer;
        }
    }
}
