package lib.kasuga.content.document;

import lib.kasuga.KasugaLib;
import lib.kasuga.core.rendering.ISpecialRenderingItem;
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

public class DocumentItem extends Item implements ISpecialRenderingItem {

    public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, KasugaLib.MODID);
    public static Supplier<DataComponentType<Map<Holder<DocumentComponentType<?>>, Object>>> DOCUMENT_COMPONENT =
            REGISTRAR.registerComponentType("document_components",
                    builder->builder.networkSynchronized(DocumentComponentRegistries.DOCUMENT_COMPONENT_BYTE_BUF)
                            .persistent(DocumentComponentRegistries.DOCUMENT_COMPONENT_PERSISTENT));

    public DocumentItem(Properties properties) {
        super(
                properties.component(DOCUMENT_COMPONENT,new HashMap<>())
        );
    }

    @Override
    public int getRenderPriority(ItemStack mainHandItem, LocalPlayer player, InteractionHand interactionHand) {
        return 2;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return (stack.has(DOCUMENT_COMPONENT) && Optional.ofNullable(stack.get(DOCUMENT_COMPONENT)).filter(Map::isEmpty).isEmpty()) ? 1 : getMaxStackSize();
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
        return (stack.has(DOCUMENT_COMPONENT) && Optional.ofNullable(stack.get(DOCUMENT_COMPONENT)).filter(Map::isEmpty).isPresent());
    }
}
