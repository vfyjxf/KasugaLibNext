package lib.kasuga.rendering.models.mc.backend.data_type;

import com.mojang.blaze3d.systems.RenderSystem;
import lib.kasuga.rendering.models.mc.source.texture.CombinedTextureManager;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;

import java.util.function.Supplier;

public class KasugaTextureStateShard extends RenderStateShard.EmptyTextureStateShard {

    public KasugaTextureStateShard(Supplier<CombinedTextureManager> textureSup) {
        super(() -> {
            CombinedTextureManager texture = textureSup.get();
            RenderSystem.setShaderTexture(0, texture.getTextureAtlas().location());
            RenderSystem.setShaderTexture(3, texture.getNormalMap().location());
            RenderSystem.setShaderTexture(4, texture.getMetallicRoughnessMap().location());
            ShaderInstance instance = RenderSystem.getShader();
            if (instance == null) return;
            instance.safeGetUniform("NormalMap").set(3);
            instance.safeGetUniform("MetallicRoughnessMap").set(4);
        }, () -> {});
    }
}
