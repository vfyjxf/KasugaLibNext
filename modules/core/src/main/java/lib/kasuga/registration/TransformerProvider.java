package lib.kasuga.registration;

import lib.kasuga.registration.core.ModifierType;

public interface TransformerProvider {
    public <L> L transform(ModifierType<L> modifierType, L element);
}
