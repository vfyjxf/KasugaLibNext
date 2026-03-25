package lib.kasuga.rendering.models.uml.structure.basic;

import lib.kasuga.rendering.models.uml.structure.skeleton.Bone;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.structure.Pair;
import lombok.Getter;

@Getter
public class BoneBinding<T extends BoneData, Q extends BoneBindingData> {

    private final Pair<Bone<T>, Float>[] weights;

    private final Q bindingData;

    public BoneBinding(Pair<Bone<T>, Float>[] weights, Q bindingData) {
        this.weights = weights;
        this.bindingData = bindingData;
        if (weights.length < 1) return;
        float w = 0;
        for (Pair<Bone<T>, Float> weight : weights) {
            w += weight.getSecond();
        }
        if (w == 0) {
            throw new IllegalArgumentException("BoneBinding weight sum cannot be zero");
        }
        if (w != 1) {
            for (int i = 0; i < weights.length; i++) {
                Pair<Bone<T>, Float> weight = weights[i];
                weights[i] = Pair.of(weight.getFirst(), weight.getSecond() / w);
            }
        }
    }
}
