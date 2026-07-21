package lib.kasuga.rendering.models.uml.dynamic.fsm;

/** How a bone-channel target is applied when flushing a blended pose to the skeleton. */
public enum ApplyMode {
    /** Replace the bone's transform entirely. */
    REPLACE,
    /** Multiply onto the current transform (additive bone delta). */
    MULTIPLY,
    /** Add a translation delta onto the current transform. */
    ADD
}
