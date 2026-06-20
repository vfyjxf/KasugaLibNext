package lib.kasuga.rendering.models.uml.dynamic.morph.results;

import lib.kasuga.rendering.models.uml.dynamic.morph.BlendMode;
import lib.kasuga.rendering.models.uml.structure.material.Material;
import lombok.Getter;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Getter
public class MaterialResult implements IMorphResult<Material> {

    private final Material original;

    private Vector4f color;
    private BlendMode colorBlendMode;

    private Vector4f specular;
    private BlendMode specularBlendMode;

    private Vector4f ambient;
    private BlendMode ambientBlendMode;

    private Vector4f edgeColor;
    private BlendMode edgeColorBlendMode;

    private Integer spriteFrame;
    private Integer spriteSetIndex;
    private Integer materialFrame;

    public MaterialResult(Material original) {
        this.original = original;
    }

    /** Multiply color multiplier (MULTIPLY mode stacks multiplicatively). */
    public void addColor(Vector4f delta, BlendMode mode) {
        if (this.color == null) { this.color = new Vector4f(delta); this.colorBlendMode = mode; }
        else this.color.mul(delta);
    }
    /** Add specular delta (ADD mode stacks additively). */
    public void addSpecular(Vector4f delta, BlendMode mode) {
        if (this.specular == null) { this.specular = new Vector4f(delta); this.specularBlendMode = mode; }
        else this.specular.add(delta);
    }
    /** Add ambient delta. */
    public void addAmbient(Vector4f delta, BlendMode mode) {
        if (this.ambient == null) { this.ambient = new Vector4f(delta); this.ambientBlendMode = mode; }
        else this.ambient.add(delta);
    }
    /** Add edge color delta. */
    public void addEdgeColor(Vector4f delta, BlendMode mode) {
        if (this.edgeColor == null) { this.edgeColor = new Vector4f(delta); this.edgeColorBlendMode = mode; }
        else this.edgeColor.add(delta);
    }
    public void setSpriteFrame(int spriteSetIndex, int frameIndex) {
        this.spriteSetIndex = spriteSetIndex; this.spriteFrame = frameIndex;
    }
    public void setMaterialFrame(int frameIndex) {
        this.materialFrame = frameIndex;
    }

    @Override
    public void reset() {
        this.color = null; this.colorBlendMode = null;
        this.specular = null; this.specularBlendMode = null;
        this.ambient = null; this.ambientBlendMode = null;
        this.edgeColor = null; this.edgeColorBlendMode = null;
        this.spriteFrame = null; this.spriteSetIndex = null;
        this.materialFrame = null;
    }

    @Override
    public boolean isEmpty() {
        return color == null && specular == null && ambient == null
                && edgeColor == null && spriteFrame == null && materialFrame == null;
    }
}
