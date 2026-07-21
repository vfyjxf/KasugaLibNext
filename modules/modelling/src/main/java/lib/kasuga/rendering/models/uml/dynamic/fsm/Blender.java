package lib.kasuga.rendering.models.uml.dynamic.fsm;

import lib.kasuga.rendering.models.uml.math.Transform;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-tick blend accumulator. Layers compose HERE (BASE/ADDITIVE/OVERRIDE with {@link BoneMask}),
 * not in {@code MorphInstance} (whose single-slot {@code applySingle} is last-write-wins). After all
 * layers compose, a {@link PoseSink} flushes exactly one write per morph id / bone / frame.
 */
public final class Blender {

    public static final class MorphAccum {

        public float baseValue;
        public float baseFactor;
        public boolean hasBase;
        public float additiveSum;
        public float overrideValue;
        public boolean hasOverride;

        void applyBase(float value, float factor) {
            this.baseValue = value;
            this.baseFactor = factor;
            this.hasBase = true;
        }

        void applyAdditive(float value, float factor) {
            this.additiveSum += value * factor;
        }

        void applyOverride(float value) {
            this.overrideValue = value;
            this.hasOverride = true;
        }

        public float value() {
            return hasOverride ? clamp01(overrideValue) : clamp01(baseValue * baseFactor + additiveSum);
        }

        public float factor() {
            return hasOverride ? 1f : (hasBase ? baseFactor : 1f);
        }
    }

    public static final class BoneAccum {

        public Transform base;
        public Transform additive;
        public Transform override;
        public boolean hasOverride;

        void applyBase(Transform transform) {
            this.base = transform.copy();
        }

        void applyAdditive(Transform transform) {
            if (this.additive == null) {
                this.additive = new Transform();
            }
            this.additive.mul(transform);
        }

        void applyOverride(Transform transform) {
            this.override = transform.copy();
            this.hasOverride = true;
        }
    }

    public static final class FrameAccum {

        public int baseFrame = -1;
        public int basePriority = -1;
        public int overrideFrame = -1;
        public boolean hasOverride;

        void applyBase(int frame, int priority) {
            if (priority >= basePriority) {
                this.baseFrame = frame;
                this.basePriority = priority;
            }
        }

        void applyOverride(int frame) {
            this.overrideFrame = frame;
            this.hasOverride = true;
        }

        public int frame() {
            return hasOverride ? overrideFrame : baseFrame;
        }
    }

    private final Map<Object, MorphAccum> morphs = new HashMap<>();
    private final Map<String, BoneAccum> bones = new HashMap<>();
    private final Map<Object, FrameAccum> frames = new HashMap<>();

    public Map<Object, MorphAccum> morphs() {
        return morphs;
    }

    public Map<String, BoneAccum> bones() {
        return bones;
    }

    public Map<Object, FrameAccum> frames() {
        return frames;
    }

    public boolean isEmpty() {
        return morphs.isEmpty() && bones.isEmpty() && frames.isEmpty();
    }

    public void reset() {
        morphs.clear();
        bones.clear();
        frames.clear();
    }

    public void applyLayer(BlendMode mode, Pose pose, float weight, BoneMask mask) {
        if (pose == null || pose.isEmpty()) {
            return;
        }
        applyMorphs(mode, pose, weight);
        applyBones(mode, pose, mask);
        applyFrames(mode, pose);
    }

    private void applyMorphs(BlendMode mode, Pose pose, float weight) {
        pose.morphs().forEach((id, m) -> {
            MorphAccum accum = morphs.computeIfAbsent(id, k -> new MorphAccum());
            switch (mode) {
                case BASE -> accum.applyBase(m.value(), m.factor() * weight);
                case ADDITIVE -> accum.applyAdditive(m.value(), m.factor() * weight);
                case OVERRIDE -> accum.applyOverride(m.value());
            }
        });
    }

    private void applyBones(BlendMode mode, Pose pose, BoneMask mask) {
        pose.bones().forEach((name, b) -> {
            if (mask != null && !mask.matches(name)) {
                return;
            }
            BoneAccum accum = bones.computeIfAbsent(name, k -> new BoneAccum());
            switch (mode) {
                case BASE -> accum.applyBase(b.transform());
                case ADDITIVE -> accum.applyAdditive(b.transform());
                case OVERRIDE -> accum.applyOverride(b.transform());
            }
        });
    }

    private void applyFrames(BlendMode mode, Pose pose) {
        pose.frames().forEach((ref, f) -> {
            FrameAccum accum = frames.computeIfAbsent(ref, k -> new FrameAccum());
            switch (mode) {
                case BASE, ADDITIVE -> accum.applyBase(f.frame(), 0);
                case OVERRIDE -> accum.applyOverride(f.frame());
            }
        });
    }

    private static float clamp01(float v) {
        return v < 0f ? 0f : Math.min(v, 1f);
    }
}
