package lib.kasuga.registration.core;

public class ConfigureAdapter<S extends ConfigureAdapter<S>> implements IModifierConfigure<S> {
    protected final IModifierConfigure<?> delegate;
    public ConfigureAdapter(IModifierConfigure<?> delegate) {
        this.delegate = delegate;
    }

    @Override
    public S configure(Modifier<?> modifier) {
        delegate.configure(modifier);
        return (S) this;
    }
}
