package lib.kasuga.registration.minecraft.item;

import lib.kasuga.registration.core.IModifierConfigure;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface ItemConfigurations<S extends ItemConfigurations<S>> extends IModifierConfigure<S> {
    public default S food() {
        return configure(ItemModifiers.FOOD);
    }

    public default S foodNutrition(int nutrition) {
        return configure(ItemModifiers.FOOD_NUTRITION.apply(nutrition));
    }

    public default S foodSaturation(float saturation) {
        return configure(ItemModifiers.FOOD_SATURATION.apply(saturation));
    }

    public default S foodFast() {
        return configure(ItemModifiers.FOOD_FAST);
    }

    public default S foodAlwaysEdible() {
        return configure(ItemModifiers.FOOD_ALWAYS_EDIBLE);
    }

    public default S stacksTo(int maxStackSize) {
        return configure(ItemModifiers.STACKS_TO.apply(maxStackSize));
    }

    public default S durability(int maxDamage) {
        return configure(ItemModifiers.DURABILITY.apply(maxDamage));
    }

    public default S craftRemainder(Item item) {
        return configure(ItemModifiers.CRAFT_REMAINDER.apply(item));
    }

    public default S rarity(Rarity rarity) {
        return configure(ItemModifiers.RARITY.apply(rarity));
    }

    public default S fireResistant() {
        return configure(ItemModifiers.FIRE_RESISTANT);
    }

    public default S jukeboxPlayable(ResourceKey<JukeboxSong> song) {
        return configure(ItemModifiers.JUKEBOX_PLAYABLE.apply(song));
    }

    public default S setNoRepair() {
        return configure(ItemModifiers.NO_REPAIR);
    }

    public default S requiredFeatures(FeatureFlag... features) {
        return configure(ItemModifiers.REQUIRED_FEATURES.apply(features));
    }

    public default S attributes(ItemAttributeModifiers attributes) {
        return configure(ItemModifiers.ATTRIBUTES.apply(attributes));
    }

    public default <T> S component(DataComponentType<T> component, T value) {
        return configure(ItemModifiers.component(component, value));
    }
}
