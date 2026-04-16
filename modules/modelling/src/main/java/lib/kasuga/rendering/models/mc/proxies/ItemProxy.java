package lib.kasuga.rendering.models.mc.proxies;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class ItemProxy<ProxiedType extends Item, ProxiedInstanceType extends ItemStack> implements ElementProxy<ProxiedType, ProxiedInstanceType> {

    private final ProxiedType item;

    public ItemProxy(ProxiedType item) {
        this.item = item;
    }

    @Override
    public boolean isValidInput(Object input) {
        return Objects.equals(input, item);
    }

    @Override
    public boolean isValidInstance(Object instance) {
        if (!(instance instanceof ItemStack stack)) return false;
        return stack.is(item);
    }
}
