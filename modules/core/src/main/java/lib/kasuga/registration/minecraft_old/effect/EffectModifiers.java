package lib.kasuga.registration.minecraft_old.effect;

import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.core.ModifierType;
import net.minecraft.Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Modifiers for effect registration configuration.
 */
public class EffectModifiers {
    
    public static final ModifierType<MobEffectCategory> TYPE_CATEGORY = new ModifierType<>();
    public static final ModifierType<Integer> TYPE_COLOR = new ModifierType<>();
    public static final ModifierType<MobEffect> TYPE_EFFECT_INSTANCE = new ModifierType<>();

    /**
     * Abstract base class for category modifiers.
     */
    public static abstract class SetCategoryModifier extends Modifier<MobEffectCategory> {
        @Override
        public ModifierType<MobEffectCategory> getType() {
            return TYPE_CATEGORY;
        }

        public static SetCategoryModifier of(MobEffectCategory category) {
            return new SetCategoryModifier() {
                @Override
                public MobEffectCategory transform(MobEffectCategory originalValue) {
                    return category;
                }
            };
        }
    }

    /**
     * Abstract base class for color modifiers.
     */
    public static abstract class SetColorModifier extends Modifier<Integer> {
        @Override
        public ModifierType<Integer> getType() {
            return TYPE_COLOR;
        }

        public static SetColorModifier of(int color) {
            return new SetColorModifier() {
                @Override
                public Integer transform(Integer originalValue) {
                    return color;
                }
            };
        }
    }

    /**
     * Abstract base class for effect instance modifiers.
     */
    public static abstract class EffectInstanceModifier extends Modifier<MobEffect> {
        @Override
        public ModifierType<MobEffect> getType() {
            return TYPE_EFFECT_INSTANCE;
        }

        public static EffectInstanceModifier of(String name, Consumer<MobEffect> modifier) {
            return new EffectInstanceModifier() {
                @Override
                public MobEffect transform(MobEffect originalValue) {
                    modifier.accept(originalValue);
                    return originalValue;
                }
            };
        }
    }

    // Category modifiers
    public static Modifier<MobEffectCategory> BENEFICIAL =
            SetCategoryModifier.of(MobEffectCategory.BENEFICIAL);
    
    public static Modifier<MobEffectCategory> HARMFUL =
            SetCategoryModifier.of(MobEffectCategory.HARMFUL);
    
    public static Modifier<MobEffectCategory> NEUTRAL =
            SetCategoryModifier.of(MobEffectCategory.NEUTRAL);

    public static Function<MobEffectCategory, Modifier<MobEffectCategory>> CATEGORY =
            Util.memoize(SetCategoryModifier::of);

    // Color modifiers
    public static Function<Integer, Modifier<Integer>> COLOR =
            Util.memoize(SetColorModifier::of);

    public static Function<Integer, Function<Integer, Function<Integer, Modifier<Integer>>>> COLOR_RGB =
            Util.memoize((r) -> Util.memoize((g) -> Util.memoize((b) -> 
                    SetColorModifier.of(r * 256 * 256 + g * 256 + b))));

    // Effect instance modifiers
    public static Function<Consumer<MobEffect>, Modifier<MobEffect>> ATTRIBUTE =
            Util.memoize((consumer) -> EffectInstanceModifier.of("attribute", consumer));
}
