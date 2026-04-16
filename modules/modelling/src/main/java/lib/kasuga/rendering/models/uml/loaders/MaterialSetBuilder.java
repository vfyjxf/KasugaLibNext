package lib.kasuga.rendering.models.uml.loaders;

import lib.kasuga.rendering.models.uml.structure.material.Material;
import lib.kasuga.rendering.models.uml.structure.material.MaterialSet;
import lib.kasuga.rendering.models.uml.structure.material.SpriteSet;
import lib.kasuga.rendering.models.uml.structure.material.Texture;
import lib.kasuga.rendering.models.uml.structure.material.data.MaterialData;
import lombok.Getter;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;

public class MaterialSetBuilder<TextureIdentifier> {

    @Getter
    private final ModelLoader loader;

    @Getter
    private final Map<TextureIdentifier, Texture> textures;

    @Getter
    private final List<Material> materials;

    private final List<TextureIdentifier> currentTextureIds;
    private final SpriteSetBuilder<TextureIdentifier> spriteSetBuilder;
    private MaterialData currentMaterialData;
    private final Map<Material, List<TriConsumer<MaterialSetBuilder<TextureIdentifier>, SpriteSetBuilder<TextureIdentifier>, Material>>> spriteBuildingFunctions;
    private final Map<Material, List<TextureIdentifier>> materialTextureIds;
    private final List<TriConsumer<MaterialSetBuilder<TextureIdentifier>, SpriteSetBuilder<TextureIdentifier>, Material>> currentSpriteBuildingFunctions;

    public MaterialSetBuilder(ModelLoader loader) {
        this.loader = loader;
        this.textures = new HashMap<>();
        this.materials = new ArrayList<>();
        this.currentTextureIds = new ArrayList<>();
        this.currentMaterialData = null;
        this.spriteSetBuilder = new SpriteSetBuilder<>(this);
        this.spriteBuildingFunctions = new HashMap<>();
        this.currentSpriteBuildingFunctions = new ArrayList<>();
        this.materialTextureIds = new HashMap<>();
    }

    public MaterialSetBuilder<TextureIdentifier> beginMaterial() {
        if (!currentTextureIds.isEmpty()) endMaterial();
        currentTextureIds.clear();
        currentMaterialData = null;
        return this;
    }

    public MaterialSetBuilder<TextureIdentifier> registerTexture(TextureIdentifier id, Texture texture) {
        textures.put(id, texture);
        return this;
    }

    public MaterialSetBuilder<TextureIdentifier> useTexture(Collection<TextureIdentifier> textureIds) {
        currentTextureIds.addAll(textureIds);
        return this;
    }

    public MaterialSetBuilder<TextureIdentifier> useTexture(TextureIdentifier textureId) {
        currentTextureIds.add(textureId);
        return this;
    }

    public MaterialSetBuilder<TextureIdentifier> setMaterialData(MaterialData materialData) {
        this.currentMaterialData = materialData;
        return this;
    }

    public MaterialSetBuilder<TextureIdentifier> addSpriteBuildingFunc(TriConsumer<MaterialSetBuilder<TextureIdentifier>, SpriteSetBuilder<TextureIdentifier>, Material> function) {
        currentSpriteBuildingFunctions.add(function);
        return this;
    }

    public Texture getTexture(TextureIdentifier id) {
        return textures.get(id);
    }

    public void nextFrame() {
        spriteSetBuilder.endSpriteSet();
    }

    public MaterialSetBuilder<TextureIdentifier> endMaterial() {
        Material material = new Material(new Texture[currentTextureIds.size()], currentMaterialData);
        int i = 0;
        for (TextureIdentifier identifier : currentTextureIds) {
            if (!textures.containsKey(identifier)) continue;
            material.getTextures()[i] = textures.get(identifier);
            i++;
        }
        material.hookTextures();
        materialTextureIds.put(material, List.copyOf(currentTextureIds));
        currentTextureIds.clear();
        spriteBuildingFunctions.put(material, List.copyOf(currentSpriteBuildingFunctions));
        currentSpriteBuildingFunctions.clear();
        currentMaterialData = null;
        materials.add(material);
        return this;
    }

    public MaterialSet endMaterialSet() {
        for (Material mat : materials) {
            List<TextureIdentifier> textureIds = materialTextureIds.get(mat);
            List<TriConsumer<MaterialSetBuilder<TextureIdentifier>, SpriteSetBuilder<TextureIdentifier>, Material>> consumers = spriteBuildingFunctions.get(mat);
            int i = 0;
            for (TextureIdentifier identifier : textureIds) {
                if (textures.containsKey(identifier)) {
                    i++;
                    continue;
                }
                Texture tex = loader.loadTexture(identifier);
                textures.put(identifier, tex);
                mat.getTextures()[i] = tex;
                i++;
            }
            for (TriConsumer<MaterialSetBuilder<TextureIdentifier>, SpriteSetBuilder<TextureIdentifier>, Material> consumer : consumers) {
                consumer.accept(this, spriteSetBuilder, mat);
            }
            if (spriteSetBuilder.getSpriteSets().isEmpty()) {
                spriteSetBuilder.endSpriteSet();
            }
            for (SpriteSet set : spriteSetBuilder.getSpriteSets()) {
                mat.addSprite(set);
            }
            mat.hookTextures();
        }
        MaterialSet materialSet = new MaterialSet(textures.values(), materials);
        return materialSet;
    }
}
