package lib.kasuga.rendering.models.mc.backend;

import com.mojang.blaze3d.vertex.*;
import lib.kasuga.rendering.models.mc.backend.data_type.MeshColorHolder;
import lib.kasuga.rendering.models.mc.backend.data_type.MeshNormalHolder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;

public class KsgBufferBuilder extends BufferBuilder {

    private final VertexFormat format;
    private final VertexFormat.Mode vertexMode;
    private final ByteBufferBuilder buffer;
    private int vertexIndex;
    private boolean building;
    private static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

    public KsgBufferBuilder(ByteBufferBuilder buffer, VertexFormat.Mode mode, VertexFormat format) {
        super(buffer, mode, format);
        this.format = format;
        this.vertexMode = mode;
        this.vertexIndex = 0;
        this.buffer = buffer;
        this.building = true;
    }

    public int getByteOffsetFor(VertexFormatElement element) {
        List<VertexFormatElement> elements = format.getElements();
        int index = elements.indexOf(element);
        if (index == -1) return -1;
        int offset = 0;
        for (int i = 0; i < index; i++) {
            VertexFormatElement e = elements.get(i);
            offset += e.byteSize();
        }
        return offset;
    }

    public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, int[] lightmap, int packedOverlay, boolean readAlpha) {
        int[] aint = quad.getVertices();
        Vec3i quadNormal = quad.getDirection().getNormal();
        Vector3f meshNormal = null;
        if (quad instanceof MeshNormalHolder holder) {
            meshNormal = holder.getMeshNormal();
        }
        Vector4f meshColor = null;
        if (quad instanceof MeshColorHolder colorHolder) {
            meshColor = colorHolder.getMeshColor();
        }
        Vector3f normal = meshNormal != null ? meshNormal : new Vector3f(
                quadNormal.getX(),
                quadNormal.getY(),
                quadNormal.getZ()
        );
        Matrix4f poseMatrix = pose.pose();
        Matrix3f poseNormal = pose.normal();
        normal = pose.transformNormal(
                normal.x(),
                normal.y(),
                normal.z(),
                normal
        );
        meshColor = meshColor != null ? meshColor : new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        int i = format.getVertexSize();
        int j = aint.length / (i / 4);
        int k = (int) (meshColor.w * 255.0f);
        int colorIndex = getByteOffsetFor(VertexFormatElement.COLOR);
        int posIndex = getByteOffsetFor(VertexFormatElement.POSITION);
        int tangentIndex = getByteOffsetFor(RenderState.TANGENT);

        try (MemoryStack memory = MemoryStack.stackPush()) {
            ByteBuffer byteBuffer = memory.malloc(format.getVertexSize());
            IntBuffer intBuffer = byteBuffer.asIntBuffer();

            for (int l = 0; l < j; ++l) {
                intBuffer.clear();
                intBuffer.put(aint, l * i / 4, i / 4);
                Vector3f position = new Vector3f(
                        byteBuffer.getFloat(posIndex),
                        byteBuffer.getFloat(posIndex + 4),
                        byteBuffer.getFloat(posIndex + 8)
                );
                float rFinal, gFinal, bFinal;

                if (readAlpha) {
                    float vertexR = (float) (byteBuffer.get(colorIndex + 3) & 0xff) / 255.0f;
                    float vertexG = (float) (byteBuffer.get(colorIndex + 2) & 0xff) / 255.0f;
                    float vertexB = (float) (byteBuffer.get(colorIndex + 1) & 0xff) / 255.0f;

                    rFinal = vertexR * brightness[l] * meshColor.x * 255.0f;
                    gFinal = vertexG * brightness[l] * meshColor.y * 255.0f;
                    bFinal = vertexB * brightness[l] * meshColor.z * 255.0f;
                } else {
                    rFinal = meshColor.x * brightness[l] * 255.0f;
                    gFinal = meshColor.y * brightness[l] * 255.0f;
                    bFinal = meshColor.z * brightness[l] * 255.0f;
                }
                int vertexAlpha = readAlpha ? (int) (meshColor.w * (float) (byteBuffer.get(colorIndex) & 255) / 255.0F * 255.0F) : k;
                int finalColor = FastColor.ARGB32.color(vertexAlpha, (int) rFinal, (int) gFinal, (int) bFinal);
                int light = this.applyBakedLighting(lightmap[l], byteBuffer);

                Vector4f tangent = new Vector4f(
                        byteBuffer.getFloat(tangentIndex),
                        byteBuffer.getFloat(tangentIndex + 4),
                        byteBuffer.getFloat(tangentIndex + 8),
                        byteBuffer.getFloat(tangentIndex + 12)
                );

                position = poseMatrix.transformPosition(position);
                float[] uvs = getUvs(byteBuffer);
                this.applyBakedNormals(normal, byteBuffer, poseNormal);
                this.addVertex(position, normal, tangent, finalColor, uvs, light, packedOverlay);
            }
        }
    }

    public int applyBakedLighting(int packedLight, ByteBuffer data) {
        int bl = packedLight & '\uffff';
        int sl = packedLight >> 16 & '\uffff';
        int offset = getByteOffsetFor(VertexFormatElement.UV2);
        int blBaked = Short.toUnsignedInt(data.getShort(offset));
        int slBaked = Short.toUnsignedInt(data.getShort(offset + 2));
        bl = Math.max(bl, blBaked);
        sl = Math.max(sl, slBaked);
        return bl | sl << 16;
    }

    private void ensureBuilding() {
        if (!building) {
            throw new IllegalStateException("Not building!");
        }
    }

    public MeshData build() {
        this.ensureBuilding();
        this.endLastVertex();
        MeshData meshData = this.storeMesh();
        this.building = false;
        this.vertexIndex = -1;
        return meshData;
    }

    @Nullable
    private MeshData storeMesh() {
        if (this.vertexIndex == 0) {
            return null;
        }
        ByteBufferBuilder.Result result = this.buffer.build();
        if (result == null) {
            return null;
        }
        int i = this.vertexMode.indexCount(this.vertexIndex);
        VertexFormat.IndexType indexType = VertexFormat.IndexType.least(this.vertexIndex);
        return new MeshData(result, new MeshData.DrawState(this.format, this.vertexIndex, i, this.vertexMode, indexType));
    }

    private long beginVertex() {
        this.ensureBuilding();
        this.endLastVertex();
        ++this.vertexIndex;
        long i = this.buffer.reserve(format.getVertexSize());
        return i;
    }

    private void endLastVertex() {
        if (this.vertexMode == VertexFormat.Mode.LINES || this.vertexMode == VertexFormat.Mode.LINE_STRIP) {
            long vertexSize = (long) format.getVertexSize();
            long i = this.buffer.reserve((int) vertexSize);
            MemoryUtil.memCopy(i - vertexSize, i, vertexSize);
            ++this.vertexIndex;
        }
    }

    private void setVec3f(long offset, Vector3f input, VertexFormatElement element) {
        int posIndex = getByteOffsetFor(element);
        MemoryUtil.memPutFloat(offset + posIndex, input.x());
        MemoryUtil.memPutFloat(offset + posIndex + 4L, input.y());
        MemoryUtil.memPutFloat(offset + posIndex + 8L, input.z());
    }

    private void setNormal(long offset, Vector3f normal) {
        int normalIndex = getByteOffsetFor(VertexFormatElement.NORMAL);
        MemoryUtil.memPutByte(offset + normalIndex, (byte) (normal.x() * 127.0f));
        MemoryUtil.memPutByte(offset + normalIndex + 1L, (byte) (normal.y() * 127.0f));
        MemoryUtil.memPutByte(offset + normalIndex + 2L, (byte) (normal.z() * 127.0f));
    }

    private void setUv(long offset, VertexFormatElement element, int[] uvs) {
        if (element.usage() != VertexFormatElement.Usage.UV) return;
        int uvOffset = getByteOffsetFor(element);
        MemoryUtil.memPutInt(offset + uvOffset, uvs[element.index()]);
        MemoryUtil.memPutInt(offset + uvOffset + 4L, uvs[element.index() + 1]);
    }

    public void setUv(long offset, float u, float v) {
        int uvOffset = getByteOffsetFor(VertexFormatElement.UV0);
        MemoryUtil.memPutFloat(offset + uvOffset, u);
        MemoryUtil.memPutFloat(offset + uvOffset + 4L, v);
    }

    private void putColor(long offest, int color) {
        int colorIndex = getByteOffsetFor(VertexFormatElement.COLOR);
        int i = FastColor.ABGR32.fromArgb32(color);
        MemoryUtil.memPutInt(offest + colorIndex, IS_LITTLE_ENDIAN ? i : Integer.reverseBytes(i));
    }

    private void setVec4f(long offset, Vector4f input, VertexFormatElement element) {
        int colorIndex = getByteOffsetFor(element);
        MemoryUtil.memPutFloat(offset + colorIndex, input.x);
        MemoryUtil.memPutFloat(offset + colorIndex + 4L, input.y);
        MemoryUtil.memPutFloat(offset + colorIndex + 8L, input.z);
        MemoryUtil.memPutFloat(offset + colorIndex + 12L, input.w);
    }

    public void addVertex(Vector3f position, Vector3f normal, Vector4f tangent, int color, float[] uv, int packedLight, int packedOverlay) {
        long i = this.beginVertex();

        setVec3f(i, position, VertexFormatElement.POSITION);
        setNormal(i, normal);
        setVec4f(i, tangent, RenderState.TANGENT);
        putColor(i, color);
        setUv(i, uv[0], uv[1]);
        putPackedOverlay(i, packedOverlay);
        putPackedLight(i, packedLight);
    }

    private void putPackedOverlay(long pointer, int overlay) {
        int uvOffset = getByteOffsetFor(VertexFormatElement.UV1);
        putPackedUv(pointer + uvOffset, overlay);
    }

    private void putPackedLight(long pointer, int packedLight) {
        int uvOffset = getByteOffsetFor(VertexFormatElement.UV2);
        putPackedUv(pointer + uvOffset, packedLight);
    }

    private static void putPackedUv(long pointer, int packedUv) {
        if (IS_LITTLE_ENDIAN) {
            MemoryUtil.memPutInt(pointer, packedUv);
        } else {
            MemoryUtil.memPutShort(pointer, (short)(packedUv & '\uffff'));
            MemoryUtil.memPutShort(pointer + 2L, (short)(packedUv >> 16 & '\uffff'));
        }

    }

    public float[] getUvs(ByteBuffer buffer) {
        int uvIndex = getByteOffsetFor(VertexFormatElement.UV0);
        return new float[] {
                buffer.getFloat(uvIndex),
                buffer.getFloat(uvIndex + 4)
        };
    }

    @Override
    public void applyBakedNormals(Vector3f generated, ByteBuffer data, Matrix3f normalTransform) {
        int normalIndex = getByteOffsetFor(VertexFormatElement.NORMAL);
        byte nx = data.get(normalIndex);
        byte ny = data.get(normalIndex + 1);
        byte nz = data.get(normalIndex + 2);
        if (nx != 0 || ny != 0 || nz != 0) {
            generated.set((float) nx / 127.0f, (float) ny / 127.0f, (float) nz / 127.0f);
            normalTransform.transform(generated);
        }
    }

    @Override
    public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay, boolean readAlpha) {
        int[] aint = quad.getVertices();
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float u, float v, int packedOverlay, int packedLight, float normalX, float normalY, float normalZ) {
        super.addVertex(x, y, z, color, u, v, packedOverlay, packedLight, normalX, normalY, normalZ);
    }
}
