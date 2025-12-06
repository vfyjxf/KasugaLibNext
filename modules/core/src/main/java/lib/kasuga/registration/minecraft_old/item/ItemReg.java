package lib.kasuga.registration.minecraft_old.item;

import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public final class ItemReg<T extends Item> extends MinecraftDeferRegistryReg<ItemReg<T>, Item, T> implements ItemConfigurations<ItemReg<T>> {

    private final Function<ItemReg<T>, Function<Item.Properties, T>> supplier;

    public static <T extends Item> ItemReg<T> of(String name, Function<Item.Properties, T> supplier) {
        return new ItemReg<T>(name, i -> supplier);
    }

    public ItemReg(String name, Function<ItemReg<T>, Function<Item.Properties, T>> supplier) {
        super(name, Registries.ITEM);
        this.supplier = supplier;
    }
    @Override
    protected T createObject(ResourceLocation id) {
        Item.Properties properties = transform(ItemModifiers.TYPE_ITEM_PROPERTIES, new Item.Properties());
        return supplier.apply(this).apply(properties);
    }
}
