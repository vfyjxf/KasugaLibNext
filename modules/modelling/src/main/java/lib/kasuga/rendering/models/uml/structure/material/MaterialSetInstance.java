package lib.kasuga.rendering.models.uml.structure.material;

import lib.kasuga.rendering.models.uml.structure.material.animators.Animator;
import lib.kasuga.rendering.models.uml.structure.material.animators.MaterialAnimation;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MaterialSetInstance {

    @Getter
    private final MaterialSet materials;

    @Getter
    private final BitSet dirtyMaterials;

    @Getter
    private final Map<SpriteSet, Integer> currentSprite;

    @Getter
    private final Map<Material, Integer> currentMatFrame;

    @Getter
    private final BitSet dirtySprites;


    public MaterialSetInstance(MaterialSet set) {
        this.materials = set;
        currentSprite = new HashMap<>();
        currentMatFrame = new HashMap<>();
        dirtyMaterials = new BitSet(materials.getMaterials().length);
        dirtySprites = new BitSet(materials.getSpriteSets().length);
        for (SpriteSet spriteSet : set.getSpriteSets()) {
            currentSprite.put(spriteSet, 0);
        }
        for (Material material : materials.getMaterials()) {
            currentMatFrame.put(material, 0);
        }
    }

    public boolean containsMaterial(Material material) {
        return materials.getIndexByMaterial().containsKey(material);
    }

    public int getCurrentSpriteFrame(Material material) {
        if (!containsMaterial(material)) return -1;
        SpriteSet set = material.getSprites().get(getCurrentMatFrame(material));
        return currentSprite.getOrDefault(set, -1);
    }

    public int getCurrentMatFrame(Material material) {
        return currentMatFrame.getOrDefault(material, -1);
    }

    public SpriteSet getSpriteSet(Material material) {
        int frame = getCurrentMatFrame(material);
        frame = Math.clamp(frame, 0, material.getSprites().size());
        return material.getSprites().get(frame);
    }

    public void setCurrentMatFrame(Material material, int frame) {
        int matIndex = materials.getIndexByMaterial().getOrDefault(material, -1);
        if (matIndex == -1) return;
        frame = Math.clamp(frame, 0, material.getSprites().size());
        if (frame != currentMatFrame.getOrDefault(material, -1))
            markMaterialDirty(material);
        currentMatFrame.put(material, frame);
    }

    public void setCurrentSpriteFrame(Material material, int frame) {
        if (!containsMaterial(material)) return;
        int currentMatFrame = getCurrentMatFrame(material);
        SpriteSet spriteSet = material.getSprites().get(currentMatFrame);
        int idx = Math.clamp(frame, 0, spriteSet.spriteSize());
        if (idx != currentSprite.getOrDefault(spriteSet, -1))
            markSpriteSetDirty(spriteSet);
        currentSprite.put(spriteSet, idx);
    }

    @Nullable
    public Sprite getSprite(Material material) {
        if (!containsMaterial(material)) return null;
        int currentMatFrame = getCurrentMatFrame(material);
        SpriteSet spriteSet = material.getSprites().get(currentMatFrame);
        return spriteSet.getSprite(currentSprite.getOrDefault(spriteSet, 0));
    }

    public void markMaterialDirty(Material material) {
        int index = materials.getIndexByMaterial().getOrDefault(material, -1);
        if (index == -1) return;
        dirtyMaterials.set(index);
    }

    public void markSpriteSetDirty(SpriteSet spriteSet) {
        Material mat = materials.getMaterial(spriteSet);
        dirtyMaterials.set(materials.getIndexByMaterial().get(mat));
        dirtySprites.set(materials.getIndexBySpriteSet().get(spriteSet));
    }

    public void clearDirty() {
        dirtySprites.clear();
        dirtyMaterials.clear();
    }

    public boolean isDirty() {
        return !dirtySprites.isEmpty() || !dirtyMaterials.isEmpty();
    }
}
