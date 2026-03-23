package lib.kasuga.rendering.models.uml.structure.skeleton;

import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
public class Bone<T extends BoneData> {

    private final String name;

    @NonNull
    @Setter
    private Transform transform;

    @Nullable
    private final T boneData;

    @Nullable
    @Setter
    private Bone<T> parent;

    @Nullable
    @Setter
    private Bone<T>[] children;

    public Bone(String name, @NonNull Transform transform, @Nullable T boneData) {
        this.name = name;
        this.boneData = boneData;
        this.transform = transform;
    }
}
