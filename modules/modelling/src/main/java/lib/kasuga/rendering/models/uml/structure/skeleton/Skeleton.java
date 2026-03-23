package lib.kasuga.rendering.models.uml.structure.skeleton;

import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.structure.Pair;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;
import java.util.Queue;

@Getter
public class Skeleton<T extends SkeletonData, R extends BoneData, Q extends AnchorData, P extends BoneBindingData> {

    private final Bone<R>[] bones;

    private final Bone<R> root;

    private final T data;

    private final Anchor<R, Q, P>[] anchors;

    private final HashMap<String, Bone<R>> boneMap;
    private final HashMap<Bone<R>, Pair<Transform, Transform>> boneTransforms;

    @NonNull
    @Setter
    private Transform transform;

    public Skeleton(Bone<R>[] bones, Bone<R> root,
                    Anchor<R, Q, P>[] anchors,
                    T data, @NonNull Transform transform) {
        this.bones = bones;
        this.root = root;
        this.data = data;
        this.transform = transform;
        this.anchors = anchors;
        this.boneMap = new HashMap<>();
        this.boneTransforms = new HashMap<>();
        for (Bone<R> bone : bones) {
            if (bone.getName().isEmpty()) continue;
            boneMap.put(bone.getName(), bone);
        }
        getInverse();
    }

    public void getInverse() {
        if (!boneTransforms.isEmpty()) return;
        Queue<Pair<Bone<R>, Transform>> queue = new java.util.LinkedList<>();
        queue.add(Pair.of(root, getTransform()));
        recursiveUpdate(queue);
    }

    private void recursiveUpdate(Queue<Pair<Bone<R>, Transform>> queue) {
        while (!queue.isEmpty()) {
            Pair<Bone<R>, Transform> pair = queue.poll();
            Bone<R> bone = pair.getFirst();
            Transform parentTransform = pair.getSecond();
            if (bone.getChildren() == null) continue;
            for (Bone<R> child : bone.getChildren()) {
                if (child == null) continue;
                Transform transform = parentTransform.copy().mul(child.getTransform());
                boneTransforms.put(child, Pair.of(transform, transform.invert()));
                queue.add(Pair.of(child, transform));
            }
        }
    }
}
