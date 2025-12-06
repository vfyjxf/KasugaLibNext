package lib.kasuga.content.document;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;

public interface DocumentComponentRenderer<T> {

    public void render(T componentValue, ItemStack stack, MultiBufferSource bufferSource, PoseStack poseStack, int light);
}
