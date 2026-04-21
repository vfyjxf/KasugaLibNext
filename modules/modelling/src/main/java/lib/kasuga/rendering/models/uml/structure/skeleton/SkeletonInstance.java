package lib.kasuga.rendering.models.uml.structure.skeleton;

import lib.kasuga.rendering.models.uml.bridge.Bridge;
import lib.kasuga.rendering.models.uml.math.binding.BoneBindingFunc;
import lib.kasuga.rendering.models.uml.math.BoneContext;
import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonInstanceData;
import lib.kasuga.structure.Pair;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

@Getter
public class SkeletonInstance {

    private final Skeleton skeleton;

    private final HashMap<Bone, Transform> transforms;
    private final HashMap<Bone, Transform> absoluteTransforms;

    private final Queue<Pair<Bone, Transform>> updateQueue = new LinkedList<>();

    @NonNull
    private Transform transform;

    private SkeletonInstanceData data;

    @Setter
    private boolean shouldUpdate;

    public SkeletonInstance(Skeleton skeleton, @Nullable Transform transform, @Nullable SkeletonInstanceData data) {
        this.skeleton = skeleton;
        this.transform = transform != null ? transform : new Transform();
        shouldUpdate = false;
        this.transforms = new HashMap<>();
        this.absoluteTransforms = new HashMap<>();
        this.data = data;
        updateTransform();
    }

    public void updateTransform() {
        updateQueue.clear();
        Bone rootBone = skeleton.getRoot();
        Transform t = transform.copy().mul(skeleton.getTransform());
        absoluteTransforms.put(rootBone, t);
        updateQueue.add(Pair.of(rootBone, t));
        recursiveUpdate();
    }

    public boolean transform(String boneName, Transform transform) {
        Bone bone = skeleton.getBoneMap().get(boneName);
        if (bone == null) return false;
        transforms.put(bone, transform);
        shouldUpdate = true;
        return true;
    }

    public boolean mulTransform(String boneName, Transform transform) {
        Bone bone = skeleton.getBoneMap().get(boneName);
        if (bone == null) return false;
        Transform current = transforms.getOrDefault(bone, new Transform());
        current.mul(transform);
        transforms.put(bone, current);
        shouldUpdate = true;
        return true;
    }

    public boolean offset(String boneName, Vector3f offset) {
        Bone bone = skeleton.getBoneMap().get(boneName);
        if (bone == null) return false;
        Transform current = transforms.getOrDefault(bone, new Transform());
        current.translate(offset);
        transforms.put(bone, current);
        shouldUpdate = true;
        return true;
    }

    public boolean rotate(String boneName, Quaternionf rotation) {
        Bone bone = skeleton.getBoneMap().get(boneName);
        if (bone == null) return false;
        Transform current = transforms.getOrDefault(bone, new Transform());
        current.mul(rotation);
        transforms.put(bone, current);
        shouldUpdate = true;
        return true;
    }

    public boolean scale(String boneName, Vector3f scale) {
        Bone bone = skeleton.getBoneMap().get(boneName);
        if (bone == null) return false;
        Transform current = transforms.getOrDefault(bone, new Transform());
        current.scale(scale.x(), scale.y(), scale.z());
        transforms.put(bone, current);
        shouldUpdate = true;
        return true;
    }

    public boolean reset(String boneName) {
        Bone bone = skeleton.getBoneMap().get(boneName);
        if (bone == null) return false;
        transforms.remove(bone);
        shouldUpdate = true;
        return true;
    }

    public boolean resetAll() {
        if (transforms.isEmpty()) return false;
        transforms.clear();
        shouldUpdate = true;
        return true;
    }

    public void transformRoot(@NonNull Transform transform) {
        this.transform = transform;
        shouldUpdate = true;
    }

    public void mulTransformRoot(@NonNull Transform transform) {
        this.transform.mul(transform);
        shouldUpdate = true;
    }

    public void offsetRoot(@NonNull Vector3f offset) {
        this.transform.translate(offset);
        shouldUpdate = true;
    }

    public void rotateRoot(@NonNull Quaternionf rotation) {
        this.transform.mul(rotation);
        shouldUpdate = true;
    }

    public void scaleRoot(@NonNull Vector3f scale) {
        this.transform.scale(scale.x(), scale.y(), scale.z());
        shouldUpdate = true;
    }

    public void resetRoot() {
        this.transform = new Transform();
        shouldUpdate = true;
    }

    public void tick() {
        if (shouldUpdate) {
            updateTransform();
            shouldUpdate = false;
        }
    }

    private void recursiveUpdate() {
        while (!updateQueue.isEmpty()) {
            Pair<Bone, Transform> boneTransformPair = updateQueue.poll();
            Bone bone = boneTransformPair.getFirst();
            Transform parentTransform = boneTransformPair.getSecond();
            if (bone.getChildren() == null) continue;
            for (Bone child : bone.getChildren()) {
                if (child == null) continue;
                Transform transform = child.getTransform();
                Transform result = parentTransform.copy().mul(transform);
                Transform anim = transforms.get(child);
                if (anim != null) result.mul(anim);
                absoluteTransforms.put(child, result);
                updateQueue.add(Pair.of(child, result));
            }
        }
    }

    public void collectBoneContexts(List<BoneContext> contexts, Vertex vertex) {
        contexts.clear();
        for (Pair<Bone, Float> weight : vertex.getBinding().getWeights()) {
            Bone bone = weight.getFirst();
            float w = weight.getSecond();
            Transform transform = transforms.getOrDefault(bone, new Transform());
            Transform absTransform = absoluteTransforms.get(bone);
            Pair<Transform, Transform> pair = skeleton.getBoneTransforms().get(bone);
            contexts.add(new BoneContext<>(bone, w, bone.getBoneData(), transform,
                    pair.getFirst(), absTransform, pair.getSecond()));
        }
    }

    public HashMap<Vertex, Vertex> getVertexTransforms(Model model, Bridge bridge) {
        HashMap<Vertex, Vertex> vertexTransforms = new HashMap<>();
        List<BoneContext> contexts = new ArrayList<>();
        for (Vertex vertex : model.getVertices()) {
            BoneBindingFunc func = bridge.getBoneBindingFunc(model, this, vertex);
            if (func == null) continue;
            collectBoneContexts(contexts, vertex);
            Vertex result = func.apply(vertex, contexts);
            vertexTransforms.put(vertex, result);
        }
        return vertexTransforms;
    }
}
