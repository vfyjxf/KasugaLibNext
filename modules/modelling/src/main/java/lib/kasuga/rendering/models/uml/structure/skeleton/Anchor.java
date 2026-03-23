package lib.kasuga.rendering.models.uml.structure.skeleton;

import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.basic.BoneBinding;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
public class Anchor<T extends BoneData, R extends AnchorData, Q extends BoneBindingData> {

    @NonNull
    private final String name;

    @NonNull
    private final BoneBinding<T, Q> binding;

    @NonNull
    @Setter
    private Transform transform;

    @Nullable
    private final R data;

    public Anchor(@NonNull String name, @NonNull BoneBinding<T, Q> binding, @NonNull Transform transform, @Nullable R data) {
        this.name = name;
        this.binding = binding;
        this.transform = transform;
        this.data = data;
    }
}
