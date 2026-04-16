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
public class ModelInstance implements AutoCloseable {

    private final Model model;

    private final SkeletonInstance skeletonInstance;

    @Nullable
    private ModelInstanceData data;

    public ModelInstance(Model model, @Nullable Transform initTransform, @Nullable ModelInstanceData data, @Nullable SkeletonInstanceData skeletonInstanceData) {
        this.model = model;
        this.data = data;
        this.skeletonInstance = new SkeletonInstance(model.getSkeleton(), initTransform, skeletonInstanceData);
    }

    public void forceUpdate() {
        skeletonInstance.setShouldUpdate(true);
    }

    public void updateImmediate() {
        forceUpdate();
        skeletonInstance.updateTransform();
    }

    @Override
    public void close() throws Exception {}
}
