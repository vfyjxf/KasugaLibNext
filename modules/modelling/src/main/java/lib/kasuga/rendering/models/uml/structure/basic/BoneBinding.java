package lib.kasuga.rendering.models.uml.structure.basic;

import lib.kasuga.rendering.models.uml.structure.skeleton.Bone;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.structure.Pair;
import lombok.Getter;

@Getter
public class BoneBinding {

    private final Pair<Bone, Float>[] weights;

    private final BoneBindingData bindingData;

    public BoneBinding(Pair<Bone, Float>[] weights, BoneBindingData bindingData) {
        this.weights = weights;
        this.bindingData = bindingData;
        if (weights.length < 1) return;
        float w = 0;
        for (Pair<Bone, Float> weight : weights) {
            w += weight.getSecond();
        }
        if (w == 0) {
            throw new IllegalArgumentException("BoneBinding weight sum cannot be zero");
        }
        if (w != 1) {
            for (int i = 0; i < weights.length; i++) {
                Pair<Bone, Float> weight = weights[i];
                weights[i] = Pair.of(weight.getFirst(), weight.getSecond() / w);
            }
        }
    }
}
