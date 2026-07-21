package lib.kasuga.rendering.models.uml.dynamic.multiplexer;

/**
 * Per-instance runtime state for a {@link Multiplexer}, held EXTERNALLY by the owner. The
 * {@link Multiplexer} definition is stateless and shareable (one per block type); this object is what
 * makes each placed block distinct. Holds the current {@link MuxVariant} plus any in-flight transition.
 */
public final class MuxState {

    private MuxVariant current;
    private MuxVariant transitionFrom;
    private MuxVariant transitionTo;
    private float elapsed;
    private float duration;

    public MuxState(MuxVariant initial) {
        this.current = initial;
    }

    /** The settled variant (the {@code from} side while a transition is in progress). */
    public MuxVariant current() {
        return current;
    }

    public boolean inTransition() {
        return transitionTo != null;
    }

    /** Variant being blended away from during a transition (== {@link #current()} when settled). */
    public MuxVariant from() {
        return transitionFrom != null ? transitionFrom : current;
    }

    /** Variant being blended toward during a transition (== {@link #current()} when settled). */
    public MuxVariant to() {
        return transitionTo != null ? transitionTo : current;
    }

    /** Cross-fade progress 0..1 (1 when settled). */
    public float alpha() {
        return duration <= 0f ? 1f : Math.min(1f, elapsed / duration);
    }

    void startTransition(MuxVariant from, MuxVariant to, float durationSeconds) {
        this.transitionFrom = from;
        this.transitionTo = to;
        this.duration = durationSeconds;
        this.elapsed = 0f;
    }

    void advance(float dt) {
        this.elapsed += dt;
    }

    boolean transitionDone() {
        return duration <= 0f || elapsed >= duration;
    }

    void commitTransition() {
        this.current = transitionTo;
        this.transitionFrom = null;
        this.transitionTo = null;
        this.elapsed = 0f;
        this.duration = 0f;
    }

    /** Instant switch (used when a transition's cross-fade is <= 0). */
    void setCurrentInstant(MuxVariant variant) {
        this.current = variant;
    }
}
