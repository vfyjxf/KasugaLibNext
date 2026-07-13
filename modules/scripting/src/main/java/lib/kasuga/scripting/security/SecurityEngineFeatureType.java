package lib.kasuga.scripting.security;

import lib.kasuga.scripting.feature.EngineFeatureType;

public class SecurityEngineFeatureType extends EngineFeatureType<SecurityEngineFeature> {

    public static final SecurityEngineFeatureType INSTANCE = new SecurityEngineFeatureType();

    public SecurityEngineFeatureType() {
        super(SecurityEngineFeature.Builder::new);
    }
}
