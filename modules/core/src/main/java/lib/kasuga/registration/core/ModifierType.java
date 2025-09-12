package lib.kasuga.registration.core;

public class ModifierType<T> {
    public final boolean skipSelf;


    public ModifierType() {
        this(false);
    }

    public ModifierType(boolean skipSelf) {
        this.skipSelf = skipSelf;
    }
}
