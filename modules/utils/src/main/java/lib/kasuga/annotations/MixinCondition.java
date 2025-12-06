package lib.kasuga.annotations;

@FunctionalInterface
public interface MixinCondition {
    public boolean shouldEnableMixin();
}
