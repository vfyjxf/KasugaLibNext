package lib.kasuga.rendering.models.uml.dynamic;

import lib.kasuga.rendering.models.uml.dynamic.morph.MorphInstance;
import lib.kasuga.rendering.models.uml.dynamic.morph.MorphResult;
import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.Mesh;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.data.ModelInstanceData;
import lib.kasuga.rendering.models.uml.structure.material.Material;
import lib.kasuga.rendering.models.uml.structure.material.MaterialSetInstance;
import lib.kasuga.rendering.models.uml.structure.material.Sprite;
import lib.kasuga.rendering.models.uml.structure.material.SpriteSet;
import lib.kasuga.rendering.models.uml.structure.material.animators.MaterialAnimation;
import lib.kasuga.rendering.models.uml.structure.skeleton.Bone;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonInstanceData;
import lib.kasuga.rendering.models.uml.util.MeshMode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.BitSet;
import java.util.Map;

@Getter
public class ModelInstance implements AutoCloseable {

    private final Model model;

    private final SkeletonInstance skeletonInstance;

    private final MaterialSetInstance materialInstance;

    @Nullable
    private ModelInstanceData data;

    @NotNull
    private MorphInstance morph;

    @Setter
    private MeshMode meshMode;

    private boolean shouldUpdate;

    public ModelInstance(Model model, @Nullable Transform initTransform,
                         @Nullable ModelInstanceData data,
                         @Nullable SkeletonInstanceData skeletonInstanceData,
                         @Nullable MaterialSetInstance materialInstance,
                         @Nullable MorphInstance morph) {
        this.model = model;
        this.data = data;
        this.meshMode = model.getMeshMode();
        this.morph = morph == null ? new MorphInstance<>(model.getMorph()) : morph;
        this.skeletonInstance = new SkeletonInstance(this, model.getSkeleton(), initTransform, skeletonInstanceData);
        this.materialInstance = materialInstance;
        this.shouldUpdate = false;
    }

    public SpriteSet getMaterialFrame(Material mat) {
        if (materialInstance == null) return mat.getSprites().getFirst();
        return materialInstance.getSpriteSet(mat);
    }

    public Sprite getMaterialSprite(Material mat) {
        if (materialInstance == null) return mat.getSprites().getFirst().getSprite(0);
        return materialInstance.getSprite(mat);
    }

    public void forceUpdate() {
        skeletonInstance.setShouldUpdate(true);
    }

    public boolean checkForUpdate() {
        shouldUpdate = skeletonInstance.checkShouldUpdate() || morph.shouldUpdate() ||
                (materialInstance != null && materialInstance.isDirty());
        return shouldUpdate;
    }

    public void updateImmediate() {
        forceUpdate();
        update();
    }

    public void update() {
        morph.update();
        skeletonInstance.updateTransform();
        updateAllMaterials();
    }

    public void updateAllMaterials() {
        if (materialInstance == null) return;
        BitSet materialSet = morph.getDirtyMaterials();
        for (int i = materialSet.nextSetBit(0); i >= 0; i = materialSet.nextSetBit(i + 1)) {
            Material mat = materialInstance.getMaterials().getMaterials()[i];
            updateMaterialFrame(mat);
            updateSpriteFrame(mat);
        }
        materialSet.clear();
    }

    public void getVertexPosition(Vertex original, Vector3f dest) {
        morph.getVertexPos(original, dest);
    }

    public void getVertexNormal(Vertex original, Mesh mesh, Vector3f dest) {
        morph.getVertexNormal(original, mesh, dest);
    }

    public void getVertexUv(Vertex original, Mesh mesh, Material material, Vector2f dest) {
        morph.getVertexUv(original, mesh, material, dest);
    }

    public void getVertexTangent(Vertex original, Vector4f dest) {
        morph.getVertexTangent(original, dest);
    }

    public void getBoneTransform(Bone bone, Transform dest) {
        morph.getBoneTransform(bone, dest);
    }

    public void getMaterialAmbient(Material material, Sprite sprite, Vector4f dest) {
        morph.getMaterialAmbient(material, sprite, dest);
    }

    public void getMaterialSpecular(Material material, Sprite sprite, Vector4f dest) {
        morph.getMaterialSpecular(material, sprite, dest);
    }

    public void getMaterialColor(Material material, Sprite sprite, Vector4f dest) {
        morph.getMaterialColor(material, sprite, dest);
    }

    public int updateMaterialFrame(Material material) {
        int frame = morph.getMaterialFrameIndex(material);
        if (materialInstance == null) return frame;
        materialInstance.setCurrentMatFrame(material, frame);
        return frame;
    }

    public int updateSpriteFrame(Material material) {
        int frame = morph.getMaterialSpriteFrame(material);
        if (materialInstance == null) return frame;
        materialInstance.setCurrentSpriteFrame(material, frame);
        return frame;
    }

    public void setMorphResultMappingType(byte type) {
        morph.setResultMappingType((byte) (type & 0x0f));
    }

    @Override
    public void close() throws Exception {}
}
