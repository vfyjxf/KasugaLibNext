package lib.kasuga.rendering.models.uml.structure;

import lib.kasuga.rendering.models.uml.structure.data.ModelData;
import lib.kasuga.rendering.models.uml.structure.basic.Mesh;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.basic.data.mesh.MeshData;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.VertexData;
import lib.kasuga.rendering.models.uml.structure.material.data.TextureData;
import lib.kasuga.rendering.models.uml.structure.skeleton.Bone;
import lib.kasuga.rendering.models.uml.structure.skeleton.Skeleton;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class Model<
        A extends ModelData,
        B extends BoneData,
        C extends MeshData,
        D extends VertexData,
        E extends TextureData,
        F extends SkeletonData,
        G extends BoneBindingData,
        H extends AnchorData> {

    private final Vertex<D, B, G>[] vertices;

    private final Mesh<C, D, E, B, G>[] meshes;

    private final Bone<B>[] bones;

    private final Skeleton<F, B, H, G> skeleton;

    @Nullable
    private final A modelData;

    public Model(Vertex<D, B, G>[] vertices,
                 Mesh<C, D, E, B, G>[] meshes,
                 Bone<B>[] bones,
                 Skeleton<F, B, H, G> skeleton,
                 @Nullable A modelData) {
        this.vertices = vertices;
        this.meshes = meshes;
        this.bones = bones;
        this.skeleton = skeleton;
        this.modelData = modelData;
    }
}
