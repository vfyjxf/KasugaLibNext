package lib.kasuga.mixins.client;


import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import lib.kasuga.core.rendering.BufferBuilderRelocator;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MultiBufferSource.BufferSource.class)
public class MixinMultiBufferSource {

    @Redirect(method = "getBuffer", at = @At(
            value = "NEW",
            target = "(Lcom/mojang/blaze3d/vertex/ByteBufferBuilder;Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;Lcom/mojang/blaze3d/vertex/VertexFormat;)Lcom/mojang/blaze3d/vertex/BufferBuilder;"
    ))
    private BufferBuilder kasugaLib$RedirectInitBufferBuilder(ByteBufferBuilder builder,
                                                    VertexFormat.Mode mode,
                                                    VertexFormat format) {
        BufferBuilder result = BufferBuilderRelocator.RELOCATOR.getBufferBuilder(builder, mode, format);
        if (result != null) return result;
        return new BufferBuilder(builder, mode, format);
    }
}
