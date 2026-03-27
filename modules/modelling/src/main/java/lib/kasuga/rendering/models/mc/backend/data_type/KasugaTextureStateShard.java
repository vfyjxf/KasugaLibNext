package lib.kasuga.rendering.models.mc.backend.data_type;

import com.mojang.blaze3d.systems.RenderSystem;
import lib.kasuga.rendering.models.mc.compat.iris.IrisCompat;
import lib.kasuga.rendering.models.mc.source.texture.CombinedTextureManager;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;

import java.util.function.Supplier;

public class KasugaTextureStateShard extends RenderStateShard.EmptyTextureStateShard {

    public KasugaTextureStateShard(Supplier<CombinedTextureManager> textureSup) {
        super(() -> {
            CombinedTextureManager texture = textureSup.get();

            ShaderInstance instance = RenderSystem.getShader();
            if (instance == null) return;
            RenderSystem.setShaderTexture(0, texture.getTextureAtlas().location());
            if (IrisCompat.isUsingShaderPack()) {
                // TODO: 补充 Iris Shader Pack 的纹理绑定和 Uniform 设置
                ExtendedShader shader = (ExtendedShader) instance;
            } else {
                instance.safeGetUniform("NormalMap").set(3);
                instance.safeGetUniform("MetallicRoughnessMap").set(4);
                instance.safeGetUniform("EmissiveMap").set(5);
                RenderSystem.setShaderTexture(3, texture.getNormalMap().location());
                RenderSystem.setShaderTexture(4, texture.getMetallicRoughnessMap().location());
                RenderSystem.setShaderTexture(5, texture.getEmissiveMap().location());
            }
        }, () -> {});
    }
}
