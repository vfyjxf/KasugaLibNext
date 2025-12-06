package lib.kasuga.core.rendering;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface ISpecialRenderingItem {
    public default int getRenderPriority(ItemStack mainHandItem, LocalPlayer player, InteractionHand interactionHand) {
        return 0;
    }
}
