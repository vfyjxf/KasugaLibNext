package lib.kasuga.rendering.models.uml.dynamic.fsm;

import lib.kasuga.rendering.models.uml.dynamic.ModelInstance;
import lib.kasuga.rendering.models.uml.dynamic.SkeletonInstance;
import lib.kasuga.rendering.models.uml.dynamic.morph.MorphInstance;
import lib.kasuga.rendering.models.uml.structure.material.Material;
import lib.kasuga.rendering.models.uml.structure.material.MaterialSetInstance;

import java.util.Map;
import java.util.Objects;

/**
 * {@link PoseSink} that flushes a {@link Blender} into the existing {@link MorphInstance} /
 * {@link SkeletonInstance} / {@link MaterialSetInstance}. The host calls {@code ModelInstance.update()}
 * once afterwards; this sink only writes.
 */
public record ModelInstancePoseSink(ModelInstance model, MaterialResolver materials) implements PoseSink {

    public ModelInstancePoseSink {
        Objects.requireNonNull(model, "model");
        if (materials == null) {
            materials = MaterialResolver.forInstance(model);
        }
    }

    public ModelInstancePoseSink(ModelInstance model) {
        this(model, MaterialResolver.forInstance(model));
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void apply(Blender blender) {
        MorphInstance morph = model.getMorph();
        SkeletonInstance skeleton = model.getSkeletonInstance();
        MaterialSetInstance materialSet = model.getMaterialInstance();

        for (Map.Entry<Object, Blender.MorphAccum> entry : blender.morphs().entrySet()) {
            Blender.MorphAccum accum = entry.getValue();
            morph.activateMorph(entry.getKey(), accum.value(), accum.factor());
        }

        for (Map.Entry<String, Blender.BoneAccum> entry : blender.bones().entrySet()) {
            Blender.BoneAccum accum = entry.getValue();
            if (accum.hasOverride) {
                skeleton.transform(entry.getKey(), accum.override);
                continue;
            }
            if (accum.base != null) {
                skeleton.transform(entry.getKey(), accum.base);
            }
            if (accum.additive != null) {
                skeleton.mulTransform(entry.getKey(), accum.additive);
            }
        }

        if (materialSet != null) {
            for (Map.Entry<Object, Blender.FrameAccum> entry : blender.frames().entrySet()) {
                int frame = entry.getValue().frame();
                if (frame < 0) {
                    continue;
                }
                Material material = materials.resolve(entry.getKey());
                if (material != null) {
                    materialSet.setCurrentMatFrame(material, frame);
                }
            }
        }
    }
}
