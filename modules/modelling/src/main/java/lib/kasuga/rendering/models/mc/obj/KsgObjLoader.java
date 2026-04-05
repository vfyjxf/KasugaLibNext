package lib.kasuga.rendering.models.mc.obj;

import lib.kasuga.rendering.models.mc.Constants;
import lib.kasuga.rendering.models.mc.backend.RenderState;
import lib.kasuga.rendering.models.mc.java_and_bedrock.IdentifierHelper;
import lib.kasuga.rendering.models.mc.java_and_bedrock.data.MCTexture;
import lib.kasuga.rendering.models.mc.java_and_bedrock.data.MCTextureData;
import lib.kasuga.rendering.models.mc.source.texture.KasugaTextureManager;
import lib.kasuga.rendering.models.uml.loaders.sources.SourceManager;
import lib.kasuga.rendering.models.uml.loaders.sources.SourceType;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.basic.data.mesh.MeshData;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.VertexData;
import lib.kasuga.rendering.models.uml.structure.data.ModelData;

import lib.kasuga.rendering.models.uml.structure.material.Texture;
import lib.kasuga.rendering.models.uml.structure.material.data.TextureData;
import lib.kasuga.rendering.models.uml.structure.skeleton.Anchor;
import lib.kasuga.rendering.models.uml.structure.skeleton.Bone;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lib.kasuga.rendering.models.uml.typo.ObjContextData;
import lib.kasuga.rendering.models.uml.typo.ObjModelLoader;
import lib.kasuga.rendering.models.uml.typo.ObjTextureData;
import lib.kasuga.structure.Pair;
import lombok.NonNull;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class KsgObjLoader extends ObjModelLoader<ModelData, BoneData, MeshData, VertexData, BoneBindingData, TextureData, SkeletonData, AnchorData, String, ResourceLocation> {

    private final HashMap<String, Pair<Material, TextureData>> textureMap;

    private final HashMap<String, ResourceLocation> loadedModels;

    public KsgObjLoader(String name) {
        super(name, true, true, true);
        textureMap = new HashMap<>();
        loadedModels = new HashMap<>();
    }

    @Override
    public Map<ResourceLocation, Model<ModelData, BoneData, MeshData, VertexData, TextureData, SkeletonData, BoneBindingData, AnchorData>> load(
            ResourceLocation identifier, String input
    ) {
        loadedModels.put(input, identifier);
        return super.load(identifier, input);
    }

    @Override
    public @Nullable VertexData getVertexData(ObjContextData data, ObjModelLoader loader, String boneName) {
        return null;
    }

    @Override
    public @Nullable MeshData getMeshData(ObjContextData data, ObjModelLoader loader) {
        return null;
    }

    @Override
    public @NonNull Texture getTexture(ObjContextData data, ObjModelLoader loader, String mtlName) {
        Pair<Material, TextureData> pair = textureMap.get(mtlName);
        Objects.requireNonNull(pair);
        return new MCTexture(mtlName, pair::getFirst, 1, 1, false, true, (MCTextureData) pair.getSecond());
    }

    @Override
    public @Nullable BoneData getBoneData(ObjModelLoader loader, String boneName) {
        return null;
    }

    @Override
    public @Nullable BoneBindingData getBoneBindingData(ObjModelLoader loader, Bone bone, Vertex vertex) {
        return null;
    }

    @Override
    public @Nullable SkeletonData getSkeletonData(ObjModelLoader loader, Bone[] bones) {
        return null;
    }

    @Override
    public @Nullable ModelData getModelData(ObjModelLoader loader) {
        return null;
    }

    @Override
    public @NotNull ResourceLocation getIdentifier(ObjModelLoader loader, String input) {
        return loadedModels.getOrDefault(input, ResourceLocation.tryBuild("kasuga_lib", "missingno"));
    }

    @Override
    public @NonNull Anchor<BoneData, AnchorData, BoneBindingData>[] getAnchors(ObjModelLoader loader, Bone<BoneData>[] bones, Bone<BoneData> rootBone) {
        return new Anchor[0];
    }

    @Override
    public void consumeTexture(ObjTextureData texture) {
        String map_kd = texture.getMap_Kd();
        if (map_kd == null) return;
        Pair<ResourceLocation, Path> pair = IdentifierHelper.getRLAndPath(map_kd);
        if (pair == null) return;
        ResourceLocation rl = pair.getFirst();
        Object identifier = pair.getSecond() == null ? rl : pair.getSecond();
        KasugaTextureManager textureManager = (KasugaTextureManager)
                ((HashMap<String, SourceManager<?>>) this.getSidedSources().get(Constants.TEXTURE_TYPE)).get("mc_layer_0");
        textureManager.load(identifier);
        MCTextureData textureData = new MCTextureData(
                identifier, textureManager
        );
        Material material = new Material(RenderState.KSG_LAYER_0, rl);
        textureMap.put(texture.getName(), Pair.of(material, textureData));
    }

    @Override
    public String getTextureContent(String mtlUrl) {
        Pair<ResourceLocation, Path> pair = IdentifierHelper.getRLAndPath(mtlUrl);
        if (pair == null) return "";
        Optional<InputStream> stream;
        if (pair.getSecond() == null) {
            ResourceLocation location = ResourceLocation.tryBuild(
                    pair.getFirst().getNamespace(),
                    "models/" + pair.getFirst().getPath() + ".mtl"
            );
            stream = IdentifierHelper.getInputStream(location);
        } else {
            stream = IdentifierHelper.getInputStream(pair.getSecond());
        }
        if (stream.isEmpty()) return "";
        try (InputStream inputStream = stream.get()) {
            InputStreamReader reader = new InputStreamReader(inputStream);
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, read);
            }
            return sb.toString();
        } catch (IOException e) {
            // warn about failed mtl loading
            return "";
        }
    }

    @Override
    public String getAsString(String input) {
        return input;
    }

    @Override
    public boolean isValidInput(Object input) {
        return input instanceof String;
    }
}
