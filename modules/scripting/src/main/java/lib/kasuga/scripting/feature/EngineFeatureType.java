package lib.kasuga.scripting.feature;

import java.util.function.Supplier;

public class EngineFeatureType<T extends EngineFeature> {

    private final Supplier<? extends EngineFeature.Builder<T>> builderFactory;

    public EngineFeatureType(Supplier<? extends EngineFeature.Builder<T>> builderFactory) {
        this.builderFactory = builderFactory;
    }

    public EngineFeature.Builder<T> builder() {
        return builderFactory.get();
    }

    public T build() {
        return builder().build();
    }
}
