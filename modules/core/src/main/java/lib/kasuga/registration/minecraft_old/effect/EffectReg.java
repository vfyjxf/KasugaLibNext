package lib.kasuga.registration.minecraft_old.effect;

import lib.kasuga.registration.core.ModifierType;
import lib.kasuga.registration.core.ScopeHelper;
import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.function.Function;

/**
 * This registration is used for mob effect registration. 
 * You can register your custom mob effect with it.
 * This replaces the old EffectReg class and follows the new registration system patterns.
 * 
 * @param <T> The mob effect class.
 */
public final class EffectReg<T extends MobEffect> extends MinecraftDeferRegistryReg<EffectReg<T>, MobEffect, T> 
        implements EffectConfigurations<EffectReg<T>> {
    
    // 定义SCOPE用于自引用
    public static final ModifierType<MobEffect> SCOPE = new ModifierType<>(true);
    
    private final Function<EffectReg<T>, EffectBuilder<T>> supplier;

    /**
     * Create an effect reg with a specific effect builder.
     * @param name the registration name.
     * @param builder the effect builder.
     * @param <T> the effect type.
     * @return a new EffectReg instance.
     */
    public static <T extends MobEffect> EffectReg<T> of(String name, EffectBuilder<T> builder) {
        return new EffectReg<>(name, reg -> builder);
    }

    /**
     * Create an effect reg with a category and color defaults.
     * @param name the registration name.
     * @param category the effect category.
     * @param color the effect color.
     * @param builder the effect builder.
     * @param <T> the effect type.
     * @return a new EffectReg instance.
     */
    public static <T extends MobEffect> EffectReg<T> of(String name, MobEffectCategory category, int color, EffectBuilder<T> builder) {
        return new EffectReg<T>(name, reg -> builder)
                .category(category)
                .color(color);
    }

    public EffectReg(String name, Function<EffectReg<T>, EffectBuilder<T>> supplier) {
        super(name, Registries.MOB_EFFECT);
        this.supplier = supplier;
        // 配置SCOPE效果
        this.configure(ScopeHelper.effect(SCOPE, this::getEntry));
    }

    @Override
    protected T createObject(ResourceLocation id) {
        // 应用属性修饰器
        MobEffectCategory category = transform(EffectModifiers.TYPE_CATEGORY, MobEffectCategory.NEUTRAL);
        int color = transform(EffectModifiers.TYPE_COLOR, 0xffffff);
        
        // 创建效果实例
        T effect = supplier.apply(this).build(category, color);
        
        // 应用属性修饰器 - 将T类型强制转换为MobEffect然后再转换回T
        @SuppressWarnings("unchecked")
        T transformedEffect = (T) transform(EffectModifiers.TYPE_EFFECT_INSTANCE, (MobEffect) effect);
        
        return transformedEffect;
    }

    /**
     * Functional interface for building effects.
     * @param <T> the effect type
     */
    @FunctionalInterface
    public interface EffectBuilder<T extends MobEffect> {
        T build(MobEffectCategory category, int color);
    }
}
