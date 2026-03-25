package lib.kasuga.rendering.models.uml.dynamic;

import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.basic.data.mesh.MeshData;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.VertexData;
import lib.kasuga.rendering.models.uml.structure.data.ModelData;
import lib.kasuga.rendering.models.uml.structure.data.ModelInstanceData;
import lib.kasuga.rendering.models.uml.structure.material.data.TextureData;
import lib.kasuga.rendering.models.uml.structure.skeleton.SkeletonInstance;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonInstanceData;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class ModelInstance<A extends ModelInstanceData, B extends ModelData, C extends BoneData, D extends MeshData, E extends VertexData, F extends SkeletonData, G extends SkeletonInstanceData, H extends TextureData, I extends AnchorData, J extends BoneBindingData> {

    private final Model<B, C, D, E, H, F, J, I> model;

    private final SkeletonInstance<G, F, C, J, I> skeletonInstance;

    @Nullable
    private A data;

    public ModelInstance(Model<B, C, D, E, H, F, J, I> model, @Nullable Transform initTransform, @Nullable A data, @Nullable G skeletonInstanceData) {
        this.model = model;
        this.data = data;
        this.skeletonInstance = new SkeletonInstance<>(model.getSkeleton(), initTransform, skeletonInstanceData);
    }

    public void forceUpdate() {
        skeletonInstance.setShouldUpdate(true);
    }

    public void updateImmediate() {
        forceUpdate();
        skeletonInstance.updateTransform();
    }
}
