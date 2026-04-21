package lib.kasuga.rendering.models.mc.typo;

import lib.kasuga.rendering.models.mc.Constants;
import lib.kasuga.rendering.models.mc.backend.RenderState;
import lib.kasuga.rendering.models.mc.java_and_bedrock.IdentifierHelper;
import lib.kasuga.rendering.models.mc.java_and_bedrock.data.MCTexture;
import lib.kasuga.rendering.models.mc.java_and_bedrock.data.MCTextureData;
import lib.kasuga.rendering.models.mc.source.texture.KasugaTextureManager;
import lib.kasuga.rendering.models.uml.loaders.sources.SourceManager;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.basic.data.mesh.MeshData;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.VertexData;
import lib.kasuga.rendering.models.uml.structure.data.ModelData;

import lib.kasuga.rendering.models.uml.structure.material.Texture;
import lib.kasuga.rendering.models.uml.structure.skeleton.Anchor;
import lib.kasuga.rendering.models.uml.structure.skeleton.Bone;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lib.kasuga.rendering.models.uml.typo.wavefont_obj.ObjContextData;
import lib.kasuga.rendering.models.uml.typo.wavefont_obj.ObjModelLoader;
import lib.kasuga.rendering.models.uml.typo.wavefont_obj.ObjTextureData;
import lib.kasuga.structure.Pair;
import lombok.NonNull;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class KsgObjLoader extends ObjModelLoader<String, ResourceLocation, String> {

    private final HashMap<String, ResourceLocation> loadedModels;

    private final HashMap<String, lib.kasuga.rendering.models.uml.structure.material.Material> materials;

    public KsgObjLoader(String name) {
        super(name, true, true, true);
        loadedModels = new HashMap<>();
        materials = new HashMap<>();
    }

    @Override
    public Map<ResourceLocation, Model> load(
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
        return materialSetBuilder().getTexture(mtlName);
    }

    @Override
    public lib.kasuga.rendering.models.uml.structure.material.@NonNull Material getMaterial(ObjContextData data, ObjModelLoader loader, String mtlName) {
        return materials.get(mtlName);
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
    public @NonNull Anchor[] getAnchors(ObjModelLoader loader, Bone[] bones, Bone rootBone) {
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
        MCTexture tex = new MCTexture(texture.getName(), () -> material, 1, 1, textureData);
        materialSetBuilder()
                .registerTexture(texture.getName(), tex)
                .useTexture(texture.getName())
                .addSpriteBuildingFunc((mtlb, sprb, mtl) -> {
                    sprb.textureId(texture.getName()).flipV(true).endSprite();
                }).endMaterial();
        materials.put(texture.getName(), materialSetBuilder().getMaterials().getLast());
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

    @Override
    public Texture loadTexture(Object textureIdentifier) {
        return getTexture(getContext().peek(), this, (String) textureIdentifier);
    }
}
