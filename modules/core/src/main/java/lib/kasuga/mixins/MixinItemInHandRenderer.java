package lib.kasuga.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import lib.kasuga.core.rendering.ISpecialRenderingItem;
import lib.kasuga.core.rendering.SpecialRenderingRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {
    @Inject(method = "renderArmWithItem",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0, shift = At.Shift.AFTER),
            cancellable = true)
    private void onRenderArmWithItem(
            AbstractClientPlayer player,
            float partialTicks,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack stack,
            float equippedProgress,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            CallbackInfo ci
    ) {
        if (!stack.isEmpty() && stack.getItem() instanceof ISpecialRenderingItem) {
            SpecialRenderingRegistry.render((ItemInHandRenderer) (Object) this, player, partialTicks, pitch, hand, swingProgress, stack, equippedProgress, poseStack, buffer, combinedLight);
            poseStack.popPose();
            ci.cancel();
        }
    }

    @Inject(method = "evaluateWhichHandsToRender", at = @At("HEAD"), cancellable = true)
    private static void onEvaluateWhichHandsToRender(
            LocalPlayer player, CallbackInfoReturnable<ItemInHandRenderer.HandRenderSelection> cir
    ) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        int mainRenderPriority = 0;
        int offRenderPriority = 0;
        if (mainHandItem.getItem() instanceof ISpecialRenderingItem mainHand) {
            mainRenderPriority = mainHand.getRenderPriority(mainHandItem, player, InteractionHand.MAIN_HAND);
        }
        if (offHandItem.getItem() instanceof ISpecialRenderingItem offHand) {
            offRenderPriority = offHand.getRenderPriority(offHandItem, player, InteractionHand.OFF_HAND);
        }
        if(mainRenderPriority == offRenderPriority)
            return;
        cir.setReturnValue(mainRenderPriority > offRenderPriority ?
                ItemInHandRenderer.HandRenderSelection.RENDER_MAIN_HAND_ONLY :
                ItemInHandRenderer.HandRenderSelection.RENDER_OFF_HAND_ONLY);
        cir.cancel();
    }
}
