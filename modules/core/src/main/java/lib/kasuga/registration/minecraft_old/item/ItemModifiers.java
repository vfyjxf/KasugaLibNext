package lib.kasuga.registration.minecraft_old.item;

import lib.kasuga.registration.TransformerProvider;
import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.core.ModifierType;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ItemLike;

import java.util.function.*;

public class ItemModifiers {

    public static ModifierType<Item.Properties> TYPE_ITEM_PROPERTIES = new ModifierType<>();
    public static ModifierType<FoodProperties.Builder> TYPE_FOOD_PROPERTIES = new ModifierType<>();



    public static abstract class SetItemPropertiesModifier extends Modifier<Item.Properties> {
        @Override
        public ModifierType<Item.Properties> getType() {
            return TYPE_ITEM_PROPERTIES;
        }

        public static SetItemPropertiesModifier of(String name, Consumer<Item.Properties> setter) {
            return new SetItemPropertiesModifier() {
                @Override
                public Item.Properties transform(Item.Properties originalValue) {
                    setter.accept(originalValue);
                    return originalValue;
                }
            };
        }

        public static SetItemPropertiesModifier ofComplex(String name, BiConsumer<TransformerProvider, Item.Properties> setter) {
            return new SetItemPropertiesModifier() {
                @Override
                public Item.Properties transform(TransformerProvider provider, Item.Properties originalValue) {
                    setter.accept(provider, originalValue);
                    return originalValue;
                }
            };
        }
    }

    public static abstract class SetFoodPropertiesModifier extends Modifier<FoodProperties.Builder> {
        @Override
        public ModifierType<FoodProperties.Builder> getType() {
            return TYPE_FOOD_PROPERTIES;
        }

        public static SetFoodPropertiesModifier of(String name, Consumer<FoodProperties.Builder> setter) {
            return new SetFoodPropertiesModifier() {
                @Override
                public FoodProperties.Builder transform(FoodProperties.Builder originalValue) {
                    setter.accept(originalValue);
                    return originalValue;
                }
            };
        }

    }


    public static Modifier<Item.Properties> FOOD =
            SetItemPropertiesModifier.ofComplex(
                    "food",
                    (p, v) -> v.food(p.transform(TYPE_FOOD_PROPERTIES, new FoodProperties.Builder()).build())
            );

    public static Function<Integer, Modifier<FoodProperties.Builder>> FOOD_NUTRITION = Util.memoize((nutrition) ->
            SetFoodPropertiesModifier.of("food_nutrition", f -> f.nutrition(nutrition))
    );

    public static Function<Float, Modifier<FoodProperties.Builder>> FOOD_SATURATION = Util.memoize((modifier) ->
            SetFoodPropertiesModifier.of("food_saturation_modifier", f -> f.saturationModifier(modifier))
    );

    public static Modifier<FoodProperties.Builder> FOOD_ALWAYS_EDIBLE = SetFoodPropertiesModifier.of("food_always_edible", FoodProperties.Builder::alwaysEdible);

    public static Modifier<FoodProperties.Builder> FOOD_FAST = SetFoodPropertiesModifier.of("food_fast", FoodProperties.Builder::fast);

    public static BiFunction<Supplier<MobEffectInstance>, Float, Modifier<FoodProperties.Builder>> FOOD_EFFECT = Util.memoize((m, f)->{
        return SetFoodPropertiesModifier.of("food_effect", u->u.effect(m, f));
    });

    public static Function<Supplier<ItemLike>, Modifier<FoodProperties.Builder>> USING_CONVERTS_TO = Util.memoize((i)->{
        return SetFoodPropertiesModifier.of("using_converts_to", u->u.usingConvertsTo(i.get()));
    });

    // Stack size
    public static Function<Integer, Modifier<Item.Properties>> STACKS_TO =
            Util.memoize((i) -> SetItemPropertiesModifier.of("stacksTo", (p) -> p.stacksTo(i)));

    // Durability
    public static Function<Integer, Modifier<Item.Properties>> DURABILITY =
            Util.memoize((i) -> SetItemPropertiesModifier.of("durability", (p) -> p.durability(i)));

    // Crafting remainder
    public static Function<Item, Modifier<Item.Properties>> CRAFT_REMAINDER =
            Util.memoize((i) -> SetItemPropertiesModifier.of("craftRemainder", (p) -> p.craftRemainder(i)));

    // Rarity
    public static Function<Rarity, Modifier<Item.Properties>> RARITY =
            Util.memoize((i) -> SetItemPropertiesModifier.of("rarity", (p) -> p.rarity(i)));

    // Fire resistant
    public static Modifier<Item.Properties> FIRE_RESISTANT =
            SetItemPropertiesModifier.of("fireResistant", Item.Properties::fireResistant);

    // Jukebox playable
    public static Function<ResourceKey<JukeboxSong>, Modifier<Item.Properties>> JUKEBOX_PLAYABLE =
            Util.memoize((i) -> SetItemPropertiesModifier.of("jukeboxPlayable", (p) -> p.jukeboxPlayable(i)));

    // No repair
    public static Modifier<Item.Properties> NO_REPAIR =
            SetItemPropertiesModifier.of("setNoRepair", Item.Properties::setNoRepair);

    // Required features
    public static Function<FeatureFlag[], Modifier<Item.Properties>> REQUIRED_FEATURES =
            Util.memoize((i) -> SetItemPropertiesModifier.of("requiredFeatures", (p) -> p.requiredFeatures(i)));

    // Attributes
    public static Function<ItemAttributeModifiers, Modifier<Item.Properties>> ATTRIBUTES =
            Util.memoize((i) -> SetItemPropertiesModifier.of("attributes", (p) -> p.attributes(i)));

    // Generic component
    public static <T> Modifier<Item.Properties> component(DataComponentType<T> component, T value) {
        return SetItemPropertiesModifier.of("component_" + component.toString(), (p) -> p.component(component, value));
    }

    public static <T> Modifier<?> component(Supplier<DataComponentType<T>> component, T value) {
        return SetItemPropertiesModifier.of("component_" + component.toString(), (p) -> p.component(component, value));
    }
}