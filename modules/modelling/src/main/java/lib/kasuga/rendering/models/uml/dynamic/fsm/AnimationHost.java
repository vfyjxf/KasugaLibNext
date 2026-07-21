package lib.kasuga.rendering.models.uml.dynamic.fsm;

/**
 * Marker owners implement to expose their {@link StateMachine} via a NeoForge capability.
 * A block entity or entity implements this and returns its (typed) machine; the cap provider unwraps it.
 */
public interface AnimationHost {
    StateMachine<?> machine();
}
