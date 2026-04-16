package lib.kasuga.rendering.models.uml.dynamic;

import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.basic.data.mesh.MeshData;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.VertexData;
import lib.kasuga.rendering.models.uml.structure.data.ModelData;
import lib.kasuga.rendering.models.uml.structure.data.ModelInstanceData;
import lib.kasuga.rendering.models.uml.structure.material.Material;
import lib.kasuga.rendering.models.uml.structure.material.MaterialSetInstance;
import lib.kasuga.rendering.models.uml.structure.material.SpriteSet;
import lib.kasuga.rendering.models.uml.structure.material.animators.MaterialAnimation;
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

    private final MaterialSetInstance materialInstance;

    @Nullable
    private ModelInstanceData data;

    public ModelInstance(Model model, @Nullable Transform initTransform,
                         @Nullable ModelInstanceData data,
                         @Nullable SkeletonInstanceData skeletonInstanceData,
                         @Nullable MaterialSetInstance materialInstance) {
        this.model = model;
        this.data = data;
        this.skeletonInstance = new SkeletonInstance(model.getSkeleton(), initTransform, skeletonInstanceData);
        this.materialInstance = materialInstance;
    }

    public SpriteSet getMaterialFrame(Material mat) {
        if (materialInstance == null || !materialInstance.getActiveAnimations().containsKey(mat))
            return mat.getSprites().getFirst();
        MaterialAnimation anim = materialInstance.getActiveAnimations().get(mat);
        return anim.getCurrentSprite();
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
