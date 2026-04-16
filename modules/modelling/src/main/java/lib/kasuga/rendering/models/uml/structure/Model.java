package lib.kasuga.rendering.models.uml.structure;

import lib.kasuga.rendering.models.uml.structure.data.ModelData;
import lib.kasuga.rendering.models.uml.structure.basic.Mesh;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.basic.data.mesh.MeshData;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.VertexData;
import lib.kasuga.rendering.models.uml.structure.material.MaterialSet;
import lib.kasuga.rendering.models.uml.structure.material.data.TextureData;
import lib.kasuga.rendering.models.uml.structure.skeleton.Bone;
import lib.kasuga.rendering.models.uml.structure.skeleton.Skeleton;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class Model {

    private final Vertex[] vertices;

    private final Mesh[] meshes;

    private final Bone[] bones;

    private final Skeleton skeleton;

    @Nullable
    private final ModelData modelData;

    private final MaterialSet materialSet;


    public Model(Vertex[] vertices,
                 Mesh[] meshes,
                 Bone[] bones,
                 Skeleton skeleton,
                 MaterialSet materialSet,
                 @Nullable ModelData modelData) {
        this.vertices = vertices;
        this.meshes = meshes;
        this.bones = bones;
        this.skeleton = skeleton;
        this.modelData = modelData;
        this.materialSet = materialSet;
    }
}
