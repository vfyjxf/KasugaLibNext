package lib.kasuga.rendering.models.uml.dynamic.fsm;

/**
 * How a {@link Layer} composes with the layers below it. Distinct axis from
 * {@code MorphInstance.BlendMode} (per-morph color multiply/add) — disambiguated by package + JavaDoc.
 */
public enum BlendMode {
    /** Bottom layer; its pose is the base. */
    BASE,
    /** Accumulates (adds) on top of the base within the {@link Blender}. */
    ADDITIVE,
    /** Masked replace: overrides base+additive for the masked channels. */
    OVERRIDE
}
