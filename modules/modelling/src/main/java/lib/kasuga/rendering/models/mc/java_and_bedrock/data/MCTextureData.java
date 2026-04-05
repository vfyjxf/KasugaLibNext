package lib.kasuga.rendering.models.mc.java_and_bedrock.data;

import lib.kasuga.rendering.models.mc.source.texture.KasugaTextureManager;
import lib.kasuga.rendering.models.uml.structure.material.data.TextureData;
import lombok.NonNull;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class MCTextureData implements TextureData, SpriteHolder {

    @NonNull
    Object identifier;

    @NonNull
    KasugaTextureManager textureManager;

    public MCTextureData(@NonNull Object identifier, @NonNull KasugaTextureManager textureManager) {
        this.identifier = identifier;
        this.textureManager = textureManager;
    }

    public TextureAtlasSprite getSprite() {
        return textureManager.get(identifier);
    }
}
