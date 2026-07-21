package lib.kasuga.rendering.models.uml.dynamic.fsm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Blend math across layers (BASE/ADDITIVE/OVERRIDE). ADDITIVE accumulates in the {@link Blender}, not in MorphInstance's single slot. */
class BlendTest {

    @Test
    void baseThenAdditiveThenOverride() {
        Blender blender = new Blender();
        blender.applyLayer(BlendMode.BASE, Pose.morph("blink", 0.5f), 1f, BoneMask.all());
        assertEquals(0.5f, blender.morphs().get("blink").value(), 1e-4f);

        blender.applyLayer(BlendMode.ADDITIVE, Pose.morph("blink", 0.2f), 1f, BoneMask.all());
        assertEquals(0.7f, blender.morphs().get("blink").value(), 1e-4f);

        blender.applyLayer(BlendMode.OVERRIDE, Pose.morph("blink", 0.3f), 1f, BoneMask.all());
        assertEquals(0.3f, blender.morphs().get("blink").value(), 1e-4f);
    }

    @Test
    void additiveClampsToOne() {
        Blender blender = new Blender();
        blender.applyLayer(BlendMode.BASE, Pose.morph("x", 0.9f), 1f, BoneMask.all());
        blender.applyLayer(BlendMode.ADDITIVE, Pose.morph("x", 0.5f), 1f, BoneMask.all());
        assertEquals(1f, blender.morphs().get("x").value(), 1e-4f);
    }

    @Test
    void weightScalesBase() {
        Blender blender = new Blender();
        blender.applyLayer(BlendMode.BASE, Pose.morph("x", 1f), 0.5f, BoneMask.all());
        assertEquals(0.5f, blender.morphs().get("x").value(), 1e-4f);
    }
}
