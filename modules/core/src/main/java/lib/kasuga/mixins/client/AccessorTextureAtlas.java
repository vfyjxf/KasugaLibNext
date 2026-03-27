package lib.kasuga.mixins.client;

import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureAtlas.class)
public interface AccessorTextureAtlas {

    @Accessor("width")
    int getWidth();

    @Accessor("height")
    int getHeight();
}
