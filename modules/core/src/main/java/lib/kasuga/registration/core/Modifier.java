package lib.kasuga.registration.core;

import lib.kasuga.registration.TransformerProvider;

public abstract class Modifier<T> {
    public abstract ModifierType<T> getType();
    public T transform(TransformerProvider provider, T originalValue) { return transform(originalValue); }
    public T transform(T originalValue) { return originalValue; }
}
