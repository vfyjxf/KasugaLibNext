package lib.kasuga.core.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

public class SpecialRenderingRegistry {

    private static final HashMap<Item, ISpecialItemRenderer> RENDERERS = new HashMap<>();

    public static void render(ItemInHandRenderer mixinItemInHandRenderer, AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight) {
        ISpecialItemRenderer i = RENDERERS.get(stack.getItem());
        if(i == null)
            return;
        poseStack.pushPose();
        i.render(mixinItemInHandRenderer, player, partialTicks, pitch, hand, swingProgress, stack, equippedProgress, poseStack, buffer, combinedLight);
        poseStack.popPose();
    }

    public static <T extends Item> void register(T item, ISpecialItemRenderer renderer) {
        RENDERERS.put(item, renderer);
    }
}
