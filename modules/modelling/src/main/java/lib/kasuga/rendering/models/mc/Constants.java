package lib.kasuga.rendering.models.mc;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import lib.kasuga.KasugaLib;
import lib.kasuga.mixins.client.AccessorOnRegisterRenderTypesEvent;
import lib.kasuga.rendering.models.mc.backend.*;
import lib.kasuga.rendering.models.mc.backend.data_type.KasugaShaderInstance;
import lib.kasuga.rendering.models.mc.compat.iris.IrisCompat;
import lib.kasuga.rendering.models.mc.java_and_bedrock.data.MCMeshData;
import lib.kasuga.rendering.models.mc.java_and_bedrock.data.be.BEModelData;
import lib.kasuga.rendering.models.mc.java_and_bedrock.loader.be.BEModelLoader;
import lib.kasuga.rendering.models.mc.obj.KsgObjLoader;
import lib.kasuga.rendering.models.mc.source.model.*;
import lib.kasuga.rendering.models.mc.source.model.json.FileJsonModelSource;
import lib.kasuga.rendering.models.mc.source.model.json.JarJsonModelSource;
import lib.kasuga.rendering.models.mc.source.model.json.JsonModelSourceManager;
import lib.kasuga.rendering.models.mc.source.model.str.FileStrModelSource;
import lib.kasuga.rendering.models.mc.source.model.str.JarStrModelSource;
import lib.kasuga.rendering.models.mc.source.model.str.StrModelSourceManager;
import lib.kasuga.rendering.models.mc.source.texture.CombinedTextureManager;
import lib.kasuga.rendering.models.mc.source.texture.FileTextureSource;
import lib.kasuga.rendering.models.mc.source.texture.JarTextureSource;
import lib.kasuga.rendering.models.uml.dynamic.ModelPipeLine;
import lib.kasuga.rendering.models.uml.loaders.sources.SourceType;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.basic.data.mesh.MeshData;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.VertexData;
import lib.kasuga.rendering.models.uml.structure.data.ModelData;
import lib.kasuga.rendering.models.uml.structure.data.ModelInstanceData;
import lib.kasuga.rendering.models.uml.structure.material.data.TextureData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonInstanceData;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.event.*;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.List;

import static lib.kasuga.rendering.models.mc.backend.RenderState.UML_VERTEX_FORMAT;

@EventBusSubscriber
public class Constants {

    public static ModelPipeLine BE_PIPELINE, OBJ_PIPELINE;
    public static CombinedTextureManager TEXTURE_BASIC;
    public static SourceType TEXTURE_TYPE, MODEL_TYPE;
    public static MCBackend MC_BACKEND;

    @SubscribeEvent
    public static void onClientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {}

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void onReloadListenerRegister(RegisterClientReloadListenersEvent event) {
        IrisCompat.onStart();
        TEXTURE_TYPE = new SourceType("texture");
        MODEL_TYPE = new SourceType("model");

        CombinedTextureManager basic = new CombinedTextureManager(
                TEXTURE_TYPE, "mc_layer_0",
                Minecraft.getInstance().getTextureManager(),
                RenderState.KSG_LAYER_0, null,
                RenderState.KSG_NORMAL_MAP, rl -> RenderState.createDefaultSprite(rl,
                () -> RenderState.getSpecularMapDefaultImage(16 ,16)),
                (rl, w, h) -> RenderState.createDefaultSprite(rl,
                        () -> RenderState.getNormalMapDefaultImage(w, h)),
                RenderState.KSG_METALLIC_MAP, rl -> RenderState.createDefaultSprite(rl,
                        () -> RenderState.getSpecularMapDefaultImage(16 ,16)),
                (rl, w, h) -> RenderState.createDefaultSprite(rl,
                        () -> RenderState.getSpecularMapDefaultImage(w, h))
        );

        FileTextureSource fileTextureSource = new FileTextureSource("file");
        JarTextureSource jarTextureSource = new JarTextureSource("jar");

        basic.registerSource(fileTextureSource);
        basic.registerSource(jarTextureSource);

        TEXTURE_BASIC = basic;

        KasugaModelManager modelManager = new KasugaModelManager(List.of(basic));
        JsonModelSourceManager jsonSource = new JsonModelSourceManager("json");
        StrModelSourceManager strSource = new StrModelSourceManager("str");

        KasugaPipeLineRouter router = new KasugaPipeLineRouter();
        router.registerByExtension(".geo.json", () -> BE_PIPELINE);
        router.registerByExtension(".obj", () -> OBJ_PIPELINE);
        modelManager.registerRouter(router);
        modelManager.registerModelScanner(new KasugaModelScanner());

        event.registerReloadListener(modelManager);

        FileJsonModelSource fileJsonModelSource = new FileJsonModelSource("file_json");
        JarJsonModelSource jarJsonModelSource = new JarJsonModelSource("jar_json");
        FileStrModelSource fileStrModelSource = new FileStrModelSource("file_str");
        JarStrModelSource jarStrModelSource = new JarStrModelSource("jar_str");


        jsonSource.registerSource(fileJsonModelSource);
        jsonSource.registerSource(jarJsonModelSource);
        strSource.registerSource(fileStrModelSource);
        strSource.registerSource(jarStrModelSource);

        BEModelLoader loader = new BEModelLoader("be_model", KasugaLib.MODID);
        MCBridge mcBridge = new MCBridge();
        MCBackend mcBackend = new MCBackend();
        MC_BACKEND = mcBackend;

        BE_PIPELINE = new ModelPipeLine.Builder<JsonObject, KsgVertexBuffer, ResourceLocation,
                ResourceLocation, String>()
                .withModelSource(jsonSource)
                .withSidedSource(basic.getType(), "mc_layer_0", basic)
                .withLoader(loader)
                .withBridge("mc_bridge", mcBridge)
                .withBackend("mc_backend", mcBackend)
                .build();

        OBJ_PIPELINE = new ModelPipeLine.Builder<String, KsgVertexBuffer, ResourceLocation,
                ResourceLocation, String>().withModelSource(strSource)
                .withSidedSource(basic.getType(), "mc_layer_0", basic)
                .withLoader(new KsgObjLoader("obj_model"))
                .withBridge("mc_bridge", mcBridge)
                .withBackend("mc_backend", mcBackend)
                .build();
    }

    @SubscribeEvent
    public static void onShaderRegister(RegisterShadersEvent event) {
        ResourceProvider provider = event.getResourceProvider();
        try {
            ShaderInstance shaderInstance = new KasugaShaderInstance(
                    provider, ResourceLocation.tryBuild("kasuga_lib", "ksglib_main"),
                    UML_VERTEX_FORMAT
            );
            event.registerShader(shaderInstance, instance -> RenderState.UML_SHADER_INSTANCE = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load shader 'ksglib_main'", e);
        }
    }

    @SubscribeEvent
    public static void onRegisterRenderBuffers(RegisterRenderBuffersEvent event) {
        RenderType typeDefault = RenderType.create(
                "kasuga_lib:uml_render_type",
                UML_VERTEX_FORMAT,
                VertexFormat.Mode.QUADS,
                256,
                true,
                true,
                RenderType.CompositeState.builder()
                        .setTextureState(RenderState.UML_TEXTURE_STATE)
                        .setShaderState(RenderState.UML_SHADER)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                        .setOutputState(RenderStateShard.MAIN_TARGET)
                        .setTexturingState(RenderStateShard.DEFAULT_TEXTURING)
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .setLineState(RenderStateShard.DEFAULT_LINE)
                        .setColorLogicState(RenderStateShard.NO_COLOR_LOGIC)
                        .createCompositeState(false)
        );
        RenderState.RENDER_TYPE = typeDefault;

        RenderType typeIris = RenderType.create(
                "kasuga_lib:iris_compat_render_type",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                true,
                true,
                RenderType.CompositeState.builder()
                        .setTextureState(RenderState.UML_TEXTURE_STATE)
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                        .setOutputState(RenderStateShard.MAIN_TARGET)
                        .setTexturingState(RenderStateShard.DEFAULT_TEXTURING)
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .setLineState(RenderStateShard.DEFAULT_LINE)
                        .setColorLogicState(RenderStateShard.NO_COLOR_LOGIC)
                        .createCompositeState(false)
        );
        RenderState.IRIS_COMPAT_RENDER_TYPE = typeIris;

        event.registerRenderBuffer(typeDefault);
        event.registerRenderBuffer(typeIris);
    }

    @SubscribeEvent
    public static void onRenderTypeRegister(RegisterNamedRenderTypesEvent event) {
        AccessorOnRegisterRenderTypesEvent accessor = (AccessorOnRegisterRenderTypesEvent) event;
        accessor.getRenderTypes().put(
                RenderState.KSG_RENDER_TYPE, new RenderTypeGroup(
                        RenderState.RENDER_TYPE,
                        RenderState.RENDER_TYPE,
                        RenderState.RENDER_TYPE
                )
        );
        accessor.getRenderTypes().put(
                RenderState.KSG_IRIS_RENDER_TYPE, new RenderTypeGroup(
                        RenderState.IRIS_COMPAT_RENDER_TYPE,
                        RenderState.IRIS_COMPAT_RENDER_TYPE,
                        RenderState.IRIS_COMPAT_RENDER_TYPE
                )
        );

//        event.register(RenderState.KSG_RENDER_TYPE, RenderState.RENDER_TYPE, RenderState.RENDER_TYPE);
    }

    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Camera camera = event.getCamera();
        Frustum frustum = event.getFrustum();
        Matrix4f modelViewMatrix = event.getModelViewMatrix();
        Matrix4f projectionMatrix = event.getProjectionMatrix();
        PoseStack poseStack = event.getPoseStack();
        int renderTick = event.getRenderTick();
        DeltaTracker partial = event.getPartialTick();
        RenderBuffers renderBuffers = Minecraft.getInstance().renderBuffers();
        MultiBufferSource.BufferSource source = renderBuffers.bufferSource();
        VertexConsumer consumer = source.getBuffer(RenderState.getRenderType());
//        RenderSystem.setShader();
        MCBackendContext context = new MCBackendContext(
                consumer, poseStack, renderBuffers,
                source, camera, frustum, modelViewMatrix,
                projectionMatrix, renderTick, partial, Minecraft.getInstance().level
        );
        testModel();
        poseStack.pushPose();
        Vec3 pos = camera.getPosition();
        poseStack.translate(- pos.x(), - pos.y(), - pos.z());
        // TODO: 这里放置各Backend
        MC_BACKEND.renderAllObjects(context);
        // TODO: 最后结束该批次
        poseStack.popPose();
        source.endBatch(RenderState.RENDER_TYPE);
    }

    private static void testModel() {
        ResourceLocation loc=  ResourceLocation.tryBuild("kasuga_lib", "models/obj/df5_frame.obj");
        ResourceLocation instanceLoc = ResourceLocation.tryBuild("kasuga_lib", "test_wheel");
        if (OBJ_PIPELINE.hasInstance(loc, instanceLoc)) return;
        OBJ_PIPELINE.createInstance(loc, instanceLoc, null, null, null);
        OBJ_PIPELINE.addToRenderer(loc, instanceLoc, "mc_bridge", "mc_backend");
//        ResourceLocation loc = ResourceLocation.tryBuild("kasuga_lib", "geometry.unknown");
//        ResourceLocation instanceLoc = ResourceLocation.tryBuild("kasuga_lib", "test_model");
//        if (BE_PIPELINE.hasInstance(loc, instanceLoc)) return;
//        BE_PIPELINE.createInstance(loc, instanceLoc, null, null, null);
//        BE_PIPELINE.addToRenderer(loc, instanceLoc, "mc_bridge", "mc_backend");
    }
}
