package lib.kasuga.scripting.feature;

public class EngineFeature {

    public static abstract class Builder<T extends EngineFeature> {
        public abstract T build();
    }
}
