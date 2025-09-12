package lib.kasuga.registration.core;

import lib.kasuga.registration.core.Modifier;

@FunctionalInterface
public interface IModifierConfigure<S> {
    S configure(Modifier<?> modifier);
}
