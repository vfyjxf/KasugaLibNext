package lib.kasuga.rendering.models.mc.java_and_bedrock.loader.je;

import com.google.gson.JsonObject;
import lib.kasuga.rendering.models.mc.java_and_bedrock.loader.TextureLayer;
import lib.kasuga.rendering.models.uml.loaders.SkeletonBuilder;
import lib.kasuga.rendering.models.uml.loaders.structural.Loader;
import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.BoneBinding;
import lib.kasuga.rendering.models.uml.structure.basic.Mesh;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.material.Texture;
import lib.kasuga.rendering.models.uml.structure.skeleton.Bone;
import lib.kasuga.rendering.models.uml.structure.skeleton.Skeleton;
import lib.kasuga.structure.Pair;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

public class JEModelLoader extends Loader<JsonObject, ResourceLocation, String> {

    private HashMap<ResourceLocation, Model> loadedModels = new HashMap<>();
    private String nameSpace;

    public JEModelLoader(String name, String nameSpace) {
        super(name);
        this.nameSpace = nameSpace;
        registerProcessor("root", new JEModelLayer());
        registerProcessor("texture_layer", new JETextureLayer());
        registerProcessor("element_layer", new JEElementLayer());
        registerProcessor("directional_layer", new JEDirectionalLayerProcessor());
    }

    @Override
    public void build(HashMap<ResourceLocation, Model> resultMap) {
        SkeletonBuilder skeletonBuilder = getBones();
        
        skeletonBuilder.addBone("root", new Transform(), null, null);

        BiFunction<
                SkeletonBuilder.AnchorRecord,
                List<Bone>,
                BoneBinding
        > anchorBindingFunc = (anchorRec, bones) -> {
            List<Pair<Bone, Float>> parentBones = new ArrayList<>();
            return new BoneBinding(
                    parentBones.toArray(new Pair[0]),
                    null,
                    null
            );
        };

        BiFunction<
                SkeletonBuilder.VertexRecord,
                List<Bone>,
                BoneBinding
        > vertexBindingFunc = (vertexRec, bones) -> {
            List<Pair<Bone, Float>> parentBones = new ArrayList<>();
            return new BoneBinding(
                    parentBones.toArray(new Pair[0]),
                    null,
                    null
            );
        };

        Skeleton skeleton = skeletonBuilder.build(
                null,
                null,
                anchorBindingFunc,
                vertexBindingFunc
        );

        Model model = new Model(
                getVertices().toArray(new Vertex[0]),
                getMeshes().toArray(new Mesh[0]),
                skeleton.getBones(),
                skeleton,
                materialSetBuilder().endMaterialSet(),
                getData()
        );

        ResourceLocation identifier = (ResourceLocation) getContext().getData("identifier");
        if (identifier != null) {
            resultMap.put(identifier, model);
        }
    }

    @Override
    public HashMap<ResourceLocation, Model> load(ResourceLocation source, JsonObject input) {
        getContext().setData("identifier", source);
        return super.load(source, input);
    }

    @Override
    public boolean isValidInput(Object input) {
        return input instanceof JsonObject;
    }

    @Override
    public Texture loadTexture(Object textureIdentifier) {
        return null;
    }
}
