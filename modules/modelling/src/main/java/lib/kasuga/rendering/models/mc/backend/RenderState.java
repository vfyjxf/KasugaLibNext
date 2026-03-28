package lib.kasuga.rendering.models.mc.backend;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import lib.kasuga.KasugaLib;
import lib.kasuga.core.rendering.BufferBuilderSupplier;
import lib.kasuga.rendering.models.mc.Constants;
import lib.kasuga.rendering.models.mc.backend.data_type.KasugaTextureStateShard;
import lib.kasuga.rendering.models.mc.compat.iris.IrisCompat;
import lib.kasuga.rendering.models.mc.source.texture.CombinedTextureManager;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;

import java.util.Objects;
import java.util.function.Supplier;

public class RenderState {

    public static final VertexFormat UML_VERTEX_FORMAT;
    public static RenderType RENDER_TYPE;
    public static RenderType IRIS_COMPAT_RENDER_TYPE;
    public static final RenderStateShard.EmptyTextureStateShard UML_TEXTURE_STATE;

    public static final VertexFormatElement TANGENT;

    public static final ResourceLocation KSG_LAYER_0 = ResourceLocation.tryBuild(KasugaLib.MODID, "textures/atlas/layer_0.png");
    public static final ResourceLocation KSG_LAYER_1 = ResourceLocation.tryBuild(KasugaLib.MODID, "textures/atlas/layer_1.png");
    public static final ResourceLocation KSG_LAYER_2 = ResourceLocation.tryBuild(KasugaLib.MODID, "textures/atlas/layer_2.png");
    public static final ResourceLocation KSG_NORMAL_MAP = ResourceLocation.tryBuild(KasugaLib.MODID, "textures/atlas/normals.png");
    public static final ResourceLocation KSG_METALLIC_MAP = ResourceLocation.tryBuild(KasugaLib.MODID, "textures/atlas/metallic.png");
    public static final ResourceLocation KSG_EMISSIVE_MAP = ResourceLocation.tryBuild(KasugaLib.MODID, "textures/atlas/emissive.png");
    public static final ResourceLocation KSG_RENDER_TYPE = ResourceLocation.tryBuild(KasugaLib.MODID, "basic_render_type");
    public static final ResourceLocation KSG_IRIS_RENDER_TYPE = ResourceLocation.tryBuild(KasugaLib.MODID, "iris_render_type");

    public static RenderStateShard.ShaderStateShard UML_SHADER;
    public static ShaderInstance UML_SHADER_INSTANCE;
    private static final ResourceMetadata SPRITE_METADATA;



    static {
        Objects.requireNonNull(KSG_LAYER_0);
        Objects.requireNonNull(KSG_LAYER_1);
        Objects.requireNonNull(KSG_LAYER_2);
        Objects.requireNonNull(KSG_NORMAL_MAP);
        Objects.requireNonNull(KSG_METALLIC_MAP);
        Objects.requireNonNull(KSG_RENDER_TYPE);
        Objects.requireNonNull(KSG_IRIS_RENDER_TYPE);
        Objects.requireNonNull(KSG_EMISSIVE_MAP);

        SPRITE_METADATA = (new ResourceMetadata.Builder())
                .put(AnimationMetadataSection.SERIALIZER,
                        new AnimationMetadataSection(ImmutableList.of(new AnimationFrame(0, -1)), 16, 16, 1, false))
                .build();

        TANGENT = VertexFormatElement.register(
                VertexFormatElement.findNextId(), 0,
                VertexFormatElement.Type.FLOAT,
                VertexFormatElement.Usage.GENERIC,
                4
        );

        UML_VERTEX_FORMAT = VertexFormat.builder()
                .add("Position", VertexFormatElement.POSITION)
                .add("Color", VertexFormatElement.COLOR)
                .add("UV0", VertexFormatElement.UV0)
                .add("UV1", VertexFormatElement.UV1)
                .add("UV2", VertexFormatElement.UV2)
                .add("Normal", VertexFormatElement.NORMAL)
                .add("Tangent", TANGENT)
                .padding(13)
                .build();

        UML_TEXTURE_STATE = new KasugaTextureStateShard(() -> (CombinedTextureManager) Constants.TEXTURE_BASIC);

        UML_SHADER =  new RenderStateShard.ShaderStateShard(() -> UML_SHADER_INSTANCE);
    }

    public static SpriteContents createDefaultSprite(ResourceLocation rl, Supplier<NativeImage> imageSup) {
        NativeImage image = imageSup.get();
        return new SpriteContents(rl, new FrameSize(image.getWidth(), image.getHeight()), image, SPRITE_METADATA);
    }

    public static NativeImage getNormalMapDefaultImage(int width, int height) {
        NativeImage image = new NativeImage(width, height, false);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setPixelRGBA(x, y, 0x00FF7F7F);  // (127, 127, 255, 0) = 0x00FF7F7F
            }
        }
        return image;
    }

    public static NativeImage getSpecularMapDefaultImage(int width, int height) {
        NativeImage image = new NativeImage(width, height, false);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setPixelRGBA(x, y, 0x00000A7F);  // (127, 10, 0, 0) = 0x00000A7F
            }
        }
        return image;
    }

    public static NativeImage getEmissiveMapDefaultImage(int width, int height) {
        NativeImage image = new NativeImage(width, height, false);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setPixelRGBA(x, y, 0);
            }
        }
        return image;
    }

    public static void addBufferBuilderRelocator(BufferBuilderSupplier supplier) {
//        BufferBuilderRelocator.RELOCATOR.addBufferBuilderSupplier(supplier);
    }

    public static RenderType getRenderType() {
        return IrisCompat.isUsingShaderPack() ? IRIS_COMPAT_RENDER_TYPE : RENDER_TYPE;
    }
}
