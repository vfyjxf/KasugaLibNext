package lib.kasuga.content.document;

import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibApplication;
import lib.kasuga.core.rendering.ISpecialRenderingItem;
import lib.kasuga.registration.minecraft.data_component.DataComponentReg;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static lib.kasuga.content.document.DocumentComponentRegistries.DOCUMENT_COMPONENT;

public class DocumentItem extends Item implements ISpecialRenderingItem {


    public DocumentItem(Properties properties) {
        super(
                properties.component(DOCUMENT_COMPONENT::getEntry,new HashMap<>())
        );
    }

    @Override
    public int getRenderPriority(ItemStack mainHandItem, LocalPlayer player, InteractionHand interactionHand) {
        return 2;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return (stack.has(DOCUMENT_COMPONENT::getEntry) && Optional.ofNullable(stack.get(DOCUMENT_COMPONENT::getEntry)).filter(Map::isEmpty).isEmpty()) ? 1 : getMaxStackSize();
    }

    protected int getMaxStackSize() {
        return 64;
    }

    public boolean canComponentStore(ItemStack stack, DocumentComponentType<?> holder) {
        return true;
    }

    public static boolean isEmptyLike(ItemStack stack) {
        if(stack.isEmpty() || !(stack.getItem() instanceof DocumentItem documentItem)) {
            return false;
        }
        return (stack.has(DOCUMENT_COMPONENT::getEntry) && Optional.ofNullable(stack.get(DOCUMENT_COMPONENT::getEntry)).filter(Map::isEmpty).isPresent());
    }
}
