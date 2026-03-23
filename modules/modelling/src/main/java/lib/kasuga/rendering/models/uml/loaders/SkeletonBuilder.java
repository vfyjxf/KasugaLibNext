package lib.kasuga.rendering.models.uml.loaders;

import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.structure.basic.BoneBinding;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.skeleton.Anchor;
import lib.kasuga.rendering.models.uml.structure.skeleton.Bone;
import lib.kasuga.rendering.models.uml.structure.skeleton.Skeleton;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

public class SkeletonBuilder<T extends BoneData, R extends AnchorData> {

    public record BoneRecord<T extends BoneData>(@NonNull String name, @NonNull Transform transform, @Nullable String parentName, @Nullable T boneData) {}

    public record AnchorRecord<T extends AnchorData>(@NonNull String name, @NonNull Transform transform, @NonNull Collection<String> parentBoneNames, @Nullable T anchorData) {}

    public record VertexRecord<T extends BoneData, R extends BoneBindingData>(@NonNull Vertex<?, T, R> vertex, @NonNull Collection<String> parentBoneNames) {}

    private final HashMap<String, BoneRecord<T>> boneRecords;

    private final HashMap<String, AnchorRecord<R>> anchorRecords;

    private final HashSet<VertexRecord<T, ?>> vertexRecords;

    public SkeletonBuilder() {
        boneRecords = new HashMap<>();
        anchorRecords = new HashMap<>();
        vertexRecords = new HashSet<>();
    }

    public void addBone(@NonNull String name, @NonNull Transform transform, @Nullable String parentName, @Nullable T boneData) {
        boneRecords.put(name, new BoneRecord<>(name, transform, parentName, boneData));
    }

    public void addAnchor(@NonNull String name, @NonNull Transform transform, @NonNull Collection<String> parentBoneNames, @Nullable R anchorData) {
        anchorRecords.put(name, new AnchorRecord<>(name, transform, parentBoneNames, anchorData));
    }

    public void addVertex(@NonNull Vertex<?, T, ?> vertex, @NonNull Collection<String> parentBoneNames) {
        vertexRecords.add(new VertexRecord<>(vertex, parentBoneNames));
    }

    public void clear() {
        boneRecords.clear();
        anchorRecords.clear();
        vertexRecords.clear();
    }

    public Skeleton<?, T, R, ?> build(SkeletonData skeletonData, @Nullable Transform rootTransform,
                                      @NonNull BiFunction<AnchorRecord<R>, List<Bone<T>>, BoneBinding<T, ?>> anchorBindingFunction,
                                      @NonNull BiFunction<VertexRecord<T, ?>, List<Bone<T>>, BoneBinding<T, ?>> vertexBindingFunction) {
        BoneRecord<T> rootRecord = null;
        for (BoneRecord<T> record : boneRecords.values()) {
            if (record.parentName == null) {
                rootRecord = record;
                break;
            }
        }
        if (rootRecord == null) {
            throw new IllegalStateException("No root bone found");
        }
        Bone<T> rootBone = new Bone<>(rootRecord.name, rootRecord.transform, rootRecord.boneData);
        Queue<Bone<T>> queue = new LinkedList<>();
        queue.add(rootBone);
        HashMap<String, Bone<T>> boneMap = new HashMap<>();
        HashMap<String, List<Bone<T>>> childrenMap = new HashMap<>();
        boneMap.put(rootBone.getName(), rootBone);
        while (!queue.isEmpty()) {
            Bone<T> bone = queue.poll();
            for (BoneRecord<T> record : boneRecords.values()) {
                if (bone.getName().equals(record.parentName)) {
                    Bone<T> childBone = new Bone<>(record.name, record.transform, record.boneData);
                    childrenMap.computeIfAbsent(bone.getName(), k -> new ArrayList<>()).add(childBone);
                    boneMap.put(childBone.getName(), childBone);
                    childBone.setParent(bone);
                    queue.add(childBone);
                }
            }
        }
        for (Bone<T> bone : boneMap.values()) {
            bone.setChildren(childrenMap.getOrDefault(bone.getName(), new ArrayList<>()).toArray(new Bone[0]));
        }
        HashMap<String, Anchor<T, R, ?>> anchorMap = new HashMap<>();
        for (AnchorRecord<R> record : anchorRecords.values()) {
            List<Bone<T>> parentBones = new ArrayList<>();
            for (String pBN : record.parentBoneNames) {
                Bone<T> parentBone = boneMap.get(pBN);
                if (parentBone != null) {
                    parentBones.add(parentBone);
                }
            }
            Anchor<T, R, ?> anchor = new Anchor<>(record.name, anchorBindingFunction.apply(record, parentBones), record.transform, record.anchorData);
            anchorMap.put(anchor.getName(), anchor);
        }
        for (VertexRecord<T, ?> record : vertexRecords) {
            List<Bone<T>> parentBones = new ArrayList<>();
            for (String pBN : record.parentBoneNames) {
                Bone<T> parentBone = boneMap.get(pBN);
                if (parentBone != null) {
                    parentBones.add(parentBone);
                }
            }
            record.vertex.setBinding((BoneBinding) vertexBindingFunction.apply(record, parentBones));
        }
        @SuppressWarnings("unchecked")
        Skeleton<?, T, R, ?> skeleton = new Skeleton<SkeletonData, T, R, BoneBindingData>(
                boneMap.values().toArray(new Bone[0]),
                rootBone,
                anchorMap.values().toArray(new Anchor[0]),
                skeletonData,
                rootTransform == null ? new Transform() : rootTransform
        );
        return skeleton;
    }
}
