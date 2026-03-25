package lib.kasuga.rendering.models.uml.structure.material;

import lib.kasuga.rendering.models.uml.structure.material.data.TextureData;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Texture<T extends TextureData> {

    private final String id;

    private final float width, height;

    private final boolean flipU, flipV;

    @Setter
    private T data;

    public Texture(String id, float textureWidth, float textureHeight, boolean flipU, boolean flipV, T data) {
        this.data = data;
        this.id = id;
        this.width = textureWidth;
        this.height = textureHeight;
        this.flipU = flipU;
        this.flipV = flipV;
    }
}
