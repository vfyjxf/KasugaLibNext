package lib.kasuga.rendering.models.uml.dynamic.morph.types;

public interface MorphType<MorphedElement, MorphOutput, Identifier> {

    boolean isValidMorphInput(MorphedElement input, float percentage, float factor);

    MorphOutput morph(MorphedElement input, float percentage, float factor);

    /** The element that this morph applies to (e.g., Vertex, Bone, Material). */
    MorphedElement getOriginal();

    Identifier getIdentifier();
}
