package lib.kasuga.rendering.models.mc.backend;

import lib.kasuga.rendering.models.mc.java_and_bedrock.data.MCTextureData;
import lib.kasuga.rendering.models.mc.java_and_bedrock.data.be.BEModelData;
import lib.kasuga.rendering.models.uml.bridge.Bridge;
import lib.kasuga.rendering.models.mc.java_and_bedrock.data.MCMeshData;
import lib.kasuga.rendering.models.mc.util.Direction;
import lib.kasuga.rendering.models.uml.math.binding.BoneBindingFunc;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.Mesh;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.basic.data.mesh.ColorizedMeshData;
import lib.kasuga.rendering.models.uml.structure.basic.data.mesh.MeshData;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.VertexData;
import lib.kasuga.rendering.models.uml.structure.data.ModelData;
import lib.kasuga.rendering.models.uml.structure.material.Texture;
import lib.kasuga.rendering.models.uml.structure.material.data.TextureData;
import lib.kasuga.rendering.models.uml.structure.skeleton.SkeletonInstance;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonInstanceData;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MCBridge implements Bridge<BEModelData, BoneData, SkeletonData, MCMeshData, VertexData,
        MCTextureData, SkeletonInstanceData, BoneBindingData, AnchorData, BakedQuad> {


    @Override
    public HashMap<Vertex, Vertex> transformVertices(Model model, SkeletonInstance skeleton, Vertex[] vertices) {
        return skeleton.getVertexTransforms(model, this);
    }

    @Override
    public Mesh<?, ?, ?, ?, ?>[] transformMeshes(Model model, SkeletonInstance skeleton, Mesh[] meshes) {
        return meshes;
    }

    @Override
    public BakedQuad[] getBackendRenderable(Model model, SkeletonInstance skeleton, HashMap vertexMap, Mesh[] meshes) {
        @SuppressWarnings("unchecked")
        HashMap<Vertex, Vertex> vertexHashMap = (HashMap<Vertex, Vertex>) vertexMap;
        List<BakedQuad> quads = new ArrayList<>();

        KsgVertexConsumer consumer = new KsgVertexConsumer(
                model.getModelData() != null &&
                        model.getModelData().isMeshTriangles()
        );
        for (Mesh mesh : meshes) {
            int color = 0xFFFFFFFF;
            Vector4f meshColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
            if (mesh.getData() instanceof ColorizedMeshData colorized) {
                color = colorized.getPackedColorRGBA();
                meshColor = colorized.getColor();
            }
            Direction direction = Direction.DOWN;
            boolean visible = true;
            boolean shade = true;
            boolean ambientOcclusion = true;
            if (mesh.getData() instanceof MCMeshData mcMeshData) {
                direction = mcMeshData.getDirection();
                visible = mcMeshData.isVisible();
                shade = mcMeshData.isShade();
                ambientOcclusion = mcMeshData.isAmbientOcclusion();
            }
            if (!visible) continue;
            consumer.setColor(color);

            for (Vertex vertex : mesh.getVertices()) {
                Vertex transformed = vertexHashMap.getOrDefault(vertex, vertex);
                Vector3f normal = transformed.getNormal(mesh);
                Vector3f pos = transformed.getPosition();

                // TODO: 这里的Sprite暂时设置为null，后续需要改成正确的Sprite
                consumer.addVertex(pos.x(), pos.y(), pos.z())
                        .setNormal(normal.x(), normal.y(), normal.z())
                        .setColor(255, 255, 255,255)
                        .setDirection(toMCDirection(direction))
                        .setShade(shade)
                        .setHasAmbientOcclusion(ambientOcclusion)
                        .setSprite(null);
                int i = 0;
                for (Texture texture : mesh.getTextures()) {
                    Vector2f uv = transformed.getUV(mesh, texture);
                    if (uv == null) continue;
                    float u0, v0, u1, v1;
                    if (texture.getData() instanceof MCTextureData textureData) {
                        TextureAtlasSprite sprite = textureData.getSprite();
                        u0 = sprite.getU0();
                        v0 = sprite.getV0();
                        u1 = sprite.getU1();
                        v1 = sprite.getV1();
                    } else {
                        u0 = 0f; v0 = 0f; u1 = 1f; v1 = 1f;
                    }
                    Vector2f uvPos = getUVPosition(uv, u0, v0, u1, v1);
                    consumer.setUv(i, uvPos.x(), uvPos.y());
                    i++;
                }
            }
            quads.add(consumer.bakedQuad(mesh.getNormal(), meshColor));
        }
        return quads.toArray(new BakedQuad[0]);
    }

    public static Vector2f getUVPosition(Vector2f uv, float u0, float v0, float u1, float v1) {
        return new Vector2f(
                uv.x() * (u1 - u0) + u0,
                uv.y() * (v1 - v0) + v0
        );
    }

    @Override
    public BoneBindingFunc<BoneData> getBoneBindingFunc(
            Model<BEModelData, BoneData, MCMeshData, VertexData, MCTextureData, SkeletonData, BoneBindingData, AnchorData> model,
            SkeletonInstance<SkeletonInstanceData, SkeletonData, BoneData, BoneBindingData, AnchorData> skeleton,
            Vertex<?, BoneData, ?> vertex
    ) {
        return BoneBindingFunc.BDEF;
    }

    public static final net.minecraft.core.Direction toMCDirection(Direction direction) {
        return switch (direction) {
            case DOWN -> net.minecraft.core.Direction.DOWN;
            case UP -> net.minecraft.core.Direction.UP;
            case NORTH -> net.minecraft.core.Direction.NORTH;
            case SOUTH -> net.minecraft.core.Direction.SOUTH;
            case WEST -> net.minecraft.core.Direction.WEST;
            case EAST -> net.minecraft.core.Direction.EAST;
        };
    }

    public static final Direction fromMCDirection(net.minecraft.core.Direction direction) {
        return switch (direction) {
            case DOWN -> Direction.DOWN;
            case UP -> Direction.UP;
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
            case EAST -> Direction.EAST;
        };
    }
}
