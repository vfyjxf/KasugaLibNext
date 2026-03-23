package lib.kasuga.rendering.models.mc.backend;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import lib.kasuga.rendering.models.mc.backend.data_type.KsgBakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.textures.UnitTextureAtlasSprite;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class KsgVertexConsumer implements VertexConsumer {

    private final Map<VertexFormatElement, Integer> ELEMENT_OFFSETS;
    private final VertexFormat.Mode mode;

    private final int[] vertexData;
    private final int vertexSize;
    private int vertexIndex;
    private boolean building;

    private int color;
    private Direction direction;
    private TextureAtlasSprite sprite;
    private boolean shade;
    private boolean hasAmbientOcclusion;

    public KsgVertexConsumer(boolean isTriangle) {
        this.mode = isTriangle ?
                VertexFormat.Mode.TRIANGLES :
                VertexFormat.Mode.QUADS;
        ELEMENT_OFFSETS = new HashMap<>();
        for (VertexFormatElement element : RenderState.UML_VERTEX_FORMAT.getElements()) {
            ELEMENT_OFFSETS.put(element, RenderState.UML_VERTEX_FORMAT.getOffset(element) / mode.primitiveLength);
        }
        vertexSize = RenderState.UML_VERTEX_FORMAT.getVertexSize();
        vertexData = new int[mode.primitiveLength * RenderState.UML_VERTEX_FORMAT.getVertexSize() / 4];

        direction = Direction.DOWN;
        sprite = UnitTextureAtlasSprite.INSTANCE;
        color = 0xFFFFFFFF;
        shade = true;
        hasAmbientOcclusion = true;
    }

    @Override
    public KsgVertexConsumer addVertex(float x, float y, float z) {
        if (this.building && ++this.vertexIndex > mode.primitiveLength) {
            throw new IllegalStateException("Expected quad export after fourth vertex");
        } else {
            this.building = true;
            int offset = getOffset(VertexFormatElement.POSITION);
            this.vertexData[offset] = Float.floatToRawIntBits(x);
            this.vertexData[offset + 1] = Float.floatToRawIntBits(y);
            this.vertexData[offset + 2] = Float.floatToRawIntBits(z);
            return this;
        }
    }

    @Override
    public KsgVertexConsumer setColor(int r, int g, int b, int a) {
        int offset = getOffset(VertexFormatElement.COLOR);
        this.vertexData[offset] = (a & 255) << 24 | (b & 255) << 16 | (g & 255) << 8 | r & 255;
        return this;
    }

    public KsgVertexConsumer setUv(int index, float u, float v) {
        VertexFormatElement uvIndex = switch (index) {
            case 0 -> VertexFormatElement.UV0;
            case 1 -> VertexFormatElement.UV1;
            case 2 -> VertexFormatElement.UV2;
            default -> throw new IllegalArgumentException("Invalid UV index: " + index);
        };
        int offset = getOffset(uvIndex);
        this.vertexData[offset] = Float.floatToRawIntBits(u);
        this.vertexData[offset + 1] = Float.floatToRawIntBits(v);
        return this;
    }

    @Override
    public KsgVertexConsumer setUv(float u, float v) {
        int offset = getOffset(VertexFormatElement.UV0);
        this.vertexData[offset] = Float.floatToRawIntBits(u);
        this.vertexData[offset + 1] = Float.floatToRawIntBits(v);
        return this;
    }

    @Override
    public KsgVertexConsumer setUv1(int u, int v) {
        int offset = getOffset(VertexFormatElement.UV1);
        this.vertexData[offset] = (((int)((short) u)) << 16) + ((short) v);
        return this;
    }

    @Override
    public KsgVertexConsumer setUv2(int u, int v) {
        int offset = getOffset(VertexFormatElement.UV2);
        this.vertexData[offset] = (((int)((short) u)) << 16) + ((short) v);
        return this;
    }

    @Override
    public KsgVertexConsumer setNormal(float x, float y, float z) {
        int offset = getOffset(VertexFormatElement.NORMAL);
        this.vertexData[offset] = (int)(x * 127.0F) & 255 | ((int)(y * 127.0F) & 255) << 8 | ((int)(z * 127.0F) & 255) << 16;
        return this;
    }

    public KsgVertexConsumer setTangent(float x, float y, float z, float w) {
        int offset = getOffset(RenderState.TANGENT);
        this.vertexData[offset] = Float.floatToRawIntBits(x);
        this.vertexData[offset + 1] = Float.floatToRawIntBits(y);
        this.vertexData[offset + 2] = Float.floatToRawIntBits(z);
        this.vertexData[offset + 3] = Float.floatToRawIntBits(w);
        return this;
    }

    public KsgVertexConsumer setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    public KsgVertexConsumer setColor(int color) {
        this.color = color;
        return this;
    }

    public KsgVertexConsumer setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
        return this;
    }

    public KsgVertexConsumer setShade(boolean shade) {
        this.shade = shade;
        return this;
    }

    public KsgVertexConsumer setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
        this.hasAmbientOcclusion = hasAmbientOcclusion;
        return this;
    }

    private int getOffset(VertexFormatElement element) {
        return (this.vertexIndex * vertexSize) / 4 + ELEMENT_OFFSETS.get(element);
    }

    public void addVertex(
            float x,
            float y,
            float z,
            int color,
            float u,
            float v,
            int packedOverlay,
            int packedLight,
            float normalX,
            float normalY,
            float normalZ,
            float tangentX,
            float tangentY,
            float tangentZ,
            float tangentW
    ) {
        this.addVertex(x, y, z);
        this.setColor(color);
        this.setUv(u, v);
        this.setOverlay(packedOverlay);
        this.setLight(packedLight);
        this.setNormal(normalX, normalY, normalZ);
        this.setTangent(tangentX, tangentY, tangentZ, tangentW);
    }

    public BakedQuad bakedQuad(@Nullable Vector3f meshNormal, @Nullable Vector4f meshColor) {
        meshNormal = meshNormal == null ? new Vector3f() : meshNormal;
        meshColor = meshColor == null ? new Vector4f(1.0f, 1.0f, 1.0f, 1.0f) : meshColor;
        if (this.building && ++ this.vertexIndex == mode.primitiveLength) {
            BakedQuad quad;
            if (this.mode == VertexFormat.Mode.TRIANGLES) {
                int offset = (mode.primitiveLength - 1) * vertexSize;
                int[] array = new int[vertexSize];
                System.arraycopy(this.vertexData, offset, array, mode.primitiveLength * vertexSize, vertexSize);
                quad = new KsgBakedQuad(
                        array,
                        meshNormal,
                        meshColor,
                        this.sprite,
                        this.shade,
                        this.hasAmbientOcclusion
                );
            } else {
                quad = new KsgBakedQuad(
                        this.vertexData.clone(),
                        meshNormal,
                        meshColor,
                        this.sprite,
                        this.shade,
                        this.hasAmbientOcclusion
                );
            }
            this.vertexIndex = 0;
            this.building = false;
            Arrays.fill(this.vertexData, 0);
            return quad;
        } else {
            throw new IllegalStateException("Not enough vertices available. Vertices in buffer: " + this.vertexIndex);
        }
    }
}
