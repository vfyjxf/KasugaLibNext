package lib.kasuga.rendering.models.uml.typo;

import lib.kasuga.rendering.models.uml.loaders.serial.ContextData;
import lib.kasuga.rendering.models.uml.loaders.serial.SerialContext;
import lib.kasuga.rendering.models.uml.loaders.serial.TextStreamLoader;
import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.Mesh;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.basic.data.mesh.MeshData;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.VertexData;
import lib.kasuga.rendering.models.uml.structure.data.ModelData;
import lib.kasuga.rendering.models.uml.structure.material.Material;
import lib.kasuga.rendering.models.uml.structure.material.Texture;
import lib.kasuga.rendering.models.uml.structure.material.data.TextureData;
import lib.kasuga.rendering.models.uml.structure.skeleton.Anchor;
import lib.kasuga.rendering.models.uml.structure.skeleton.Bone;
import lib.kasuga.rendering.models.uml.structure.skeleton.Skeleton;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lib.kasuga.rendering.models.uml.typo.mtl.MtlLoader;
import lib.kasuga.rendering.models.uml.typo.processors.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public abstract class ObjModelLoader<
        InputType, OutputIdentifier, TextureIdentifier> extends TextStreamLoader<InputType, OutputIdentifier, TextureIdentifier, ObjContextData> {

    @Setter
    @Getter
    private String mtllibURL;

    @Getter
    private final ArrayList<Vertex> vertices;

    @Getter
    private final ArrayList<Mesh> meshes;

    @Getter
    private final ArrayList<Bone> bones;

    @Getter
    private InputType currentInput;

    @Getter
    private final MtlLoader mtlLoader;

    public ObjModelLoader(String name, boolean trim, boolean useDefaultProcessors, boolean mtlUseDefaultProcessors) {
        super(name, "\n", trim);
        mtllibURL = null;
        vertices = new ArrayList<>();
        meshes = new ArrayList<>();
        bones = new ArrayList<>();
        mtlLoader = new MtlLoader(this, mtlUseDefaultProcessors);
        if (useDefaultProcessors) useDefaultProcessors();
    }

    public void useDefaultProcessors() {
        registerProcessor("v", new ObjVertexProcessor());
        registerProcessor("vt", new ObjUvProcessor());
        registerProcessor("o", new ObjObjectProcessor());
        registerProcessor("g", new ObjGroupProcessor());
        registerProcessor("vn", new ObjNormalProcessor());
        registerProcessor("usemtl", new ObjUseMtlProcessor());
        registerProcessor("f", new ObjBindingProcessor());
        registerProcessor("mtllib", new ObjMtlProcessor());
        registerProcessor("s", new ObjSmoothShadeProcessor());
    }

    @Override
    public Map<OutputIdentifier, Model> load(OutputIdentifier identifier, InputType input) {
        currentInput = input;
        return super.load(identifier, input);
    }

    public void loadMtl() {
        if (mtllibURL == null) return;
        String mtlContent = getTextureContent(mtllibURL);
        if (mtlContent == null) return;
        mtlLoader.readMtlFile(mtlContent);
        mtllibURL = null;
    }

    public abstract @Nullable VertexData getVertexData(ObjContextData data, ObjModelLoader loader, String boneName);

    public abstract @Nullable MeshData getMeshData(ObjContextData data, ObjModelLoader loader);

    public abstract @NonNull Texture getTexture(ObjContextData data, ObjModelLoader loader, String mtlName);

    public abstract @NonNull Material getMaterial(ObjContextData data, ObjModelLoader loader, String mtlName);

    public abstract @Nullable BoneData getBoneData(ObjModelLoader loader, String boneName);

    public abstract @Nullable BoneBindingData getBoneBindingData(ObjModelLoader loader, Bone bone, Vertex vertex);

    public abstract @Nullable SkeletonData getSkeletonData(ObjModelLoader loader, Bone[] bones);

    public abstract @Nullable ModelData getModelData(ObjModelLoader loader);

    public abstract @NonNull OutputIdentifier getIdentifier(ObjModelLoader loader, InputType input);

    public abstract @NonNull Anchor[] getAnchors(ObjModelLoader loader, Bone[] bones, Bone rootBone);

    public abstract void consumeTexture(ObjTextureData texture);

    public abstract String getTextureContent(String mtlUrl);

    public void endHighestGroup(SerialContext<ObjContextData> context) {
        if (context.isEmpty()) return;
        Stack<ObjContextData> tempStack = new Stack<>();
        ObjContextData cache;
        String boneName = null;
        while (!context.isEmpty()) {
            cache = context.pop();
            if (cache.isGroup()) {
                boneName = cache.getName();
                break;
            } else {
                tempStack.push(cache);
            }
        }
        if (boneName == null) boneName = "bone_" + tempStack.hashCode();
        Bone bone = new Bone(boneName, new Transform(), getBoneData(this, boneName));
        while (!tempStack.isEmpty()) {
            cache = tempStack.pop();
            cache.buildVertexAndMesh(this, bone);
        }
        this.bones.add(bone);
    }

    public void clear() {
        this.mtllibURL = null;
        this.currentInput = null;
        this.vertices.clear();
        this.meshes.clear();
        this.bones.clear();
    }

    @Override
    public void build(HashMap<OutputIdentifier, Model> result) {
        while (!getContext().isEmpty()) {
            endHighestGroup(getContext());
        }
        Bone rootBone;
        if (bones.size() == 1) rootBone = bones.getFirst();
        else rootBone = new Bone("root", new Transform(), getBoneData(this, "root"));
        if (bones.size() > 1) {
            rootBone.setChildren(bones.toArray(new Bone[0]));
            bones.forEach(b -> b.setParent(rootBone));
        }
        Bone[] b = new Bone[bones.size() > 1 ? bones.size() + 1 : 1];
        if (bones.size() > 1) {
            b[0] = rootBone;
            for (int i = 0; i < bones.size(); i++) {
                b[i + 1] = bones.get(i);
            }
        } else {
            b[0] = rootBone;
        }
        Skeleton skeleton = new Skeleton(
                b, rootBone, getAnchors(this, b, rootBone), getSkeletonData(this, b), new Transform()
        );
        Model model = new Model(
                vertices.toArray(new Vertex[0]),
                meshes.toArray(new Mesh[0]),
                b,
                skeleton,
                materialSetBuilder().endMaterialSet(),
                getModelData(this)
        );
        result.put(getIdentifier(this, currentInput), model);
        clear();
    }
}
