package lib.kasuga.registration.minecraft_old.effect;

import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.IModifierConfigure;
import lib.kasuga.registration.core.IAdaptedObject;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.function.Consumer;

/**
 * Configuration interface for effect registration.
 * This interface provides common configuration methods for mob effects.
 * 
 * @param <S> the type of the effect registration
 */
public interface EffectConfigurations<S extends EffectConfigurations<S>> extends IModifierConfigure<S> {
    
    /**
     * Set the effect category to beneficial.
     * @return self
     */
    default S beneficial() {
        return configure(EffectModifiers.BENEFICIAL);
    }
    
    /**
     * Set the effect category to harmful.
     * @return self
     */
    default S harmful() {
        return configure(EffectModifiers.HARMFUL);
    }
    
    /**
     * Set the effect category to neutral.
     * @return self
     */
    default S neutral() {
        return configure(EffectModifiers.NEUTRAL);
    }
    
    /**
     * Set the effect category.
     * @param category the effect category
     * @return self
     */
    default S category(MobEffectCategory category) {
        return configure(EffectModifiers.CATEGORY.apply(category));
    }
    
    /**
     * Set the effect color from RGB values.
     * @param r red component (0-255)
     * @param g green component (0-255)
     * @param b blue component (0-255)
     * @return self
     */
    default S color(int r, int g, int b) {
        return configure(EffectModifiers.COLOR_RGB.apply(r).apply(g).apply(b));
    }
    
    /**
     * Set the effect color from integer value.
     * @param color the color value (0x000000 - 0xffffff)
     * @return self
     */
    default S color(int color) {
        return configure(EffectModifiers.COLOR.apply(color));
    }
    
    /**
     * Apply attributes or other modifications to the effect instance.
     * @param attributeModifier the attribute modifier lambda
     * @return self
     */
    default S attribute(Consumer<MobEffect> attributeModifier) {
        return configure(EffectModifiers.ATTRIBUTE.apply(attributeModifier));
    }
    
    /**
     * Adapter pattern support for unified configuration across different registration types.
     */
    static abstract class ConsumeAdapter implements EffectConfigurations<ConsumeAdapter>, IAdaptedObject<Reg<?, ?>> {}
    
    /**
     * Adapt a consumer for use with different registration types.
     * @param s the consumer to adapt
     * @param <T> the registration type
     * @return an adapted consumer
     */
    public static <T extends Reg<T, ?>> Consumer<T> adaptConsume(Consumer<ConsumeAdapter> s) {
        return (i) -> s.accept(new ConsumeAdapter(){
            @Override
            public Reg<?, ?> getOriginal() { 
                return i; 
            }
            
            @Override
            public ConsumeAdapter configure(lib.kasuga.registration.core.Modifier<?> modifier) {
                i.configure(modifier);
                return this;
            }
        });
    }
}
