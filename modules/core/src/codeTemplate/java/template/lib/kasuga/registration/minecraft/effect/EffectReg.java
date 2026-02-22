package template.lib.kasuga.registration.minecraft.effect;

import lib.kasuga.internal.generator.annotations.CodeTemplate;
import lib.kasuga.internal.generator.annotations.RegGenerator;
import lib.kasuga.internal.generator.facades.RegFacade;
import lib.kasuga.registration.core.ModifierType;
import lib.kasuga.registration.core.ScopeHelper;
import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This registration is used for mob effect registration. 
 * You can register your custom mob effect with it.
 * This replaces the old EffectReg class and follows the new registration system patterns.
 * 
 * @param <T> The mob effect class.
 */
@CodeTemplate(generator = "Reg")
@RegGenerator(
        modifiers = {
                @RegGenerator.Modifier(
                        type = "Category",
                        target = MobEffectCategory.class
                ),
                @RegGenerator.Modifier(
                        type = "Color",
                        target = Integer.class
                ),
                @RegGenerator.Modifier(
                        type = "EffectInstance",
                        target = MobEffect.class
                )
        }
)
public final class EffectReg<T extends MobEffect> extends MinecraftDeferRegistryReg<EffectReg<T>, MobEffect, T> {
    
    // 定义SCOPE用于自引用
    public static final ModifierType<MobEffect> SCOPE = new ModifierType<>(true);
    
    private final Function<EffectReg<T>, EffectBuilder<T>> supplier;

    public static <T extends MobEffect> EffectReg<T> of(String name, EffectBuilder<T> builder) {
        return new EffectReg<>(name, reg -> builder);
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
        MobEffectCategory category = RegFacade.transformObject("Category", MobEffectCategory.NEUTRAL);
        int color = RegFacade.transformObject("Color", 0xffffff);
        
        // 创建效果实例
        T effect = supplier.apply(this).build(category, color);
        
        // 应用属性修饰器 - 将T类型强制转换为MobEffect然后再转换回T
        @SuppressWarnings("unchecked")
        T transformedEffect = (T) RegFacade.transformObject("EffectInstance", (MobEffect) effect);
        
        return transformedEffect;
    }

    @RegGenerator.ModifyFunction(type = "Category")
    public MobEffectCategory category(MobEffectCategory originalValue, MobEffectCategory category) {
        return category;
    }

    @RegGenerator.ModifyFunction(type = "Color")
    public Integer color(Integer originalValue, int color) {
        return color;
    }

//    @RegGenerator.ModifyFunction(type = "Color")
//    public Integer color(Integer originalValue, int r, int g, int b) {
//        return r * 256 * 256 + g * 256 + b;
//    }

    @RegGenerator.ModifyFunction(type = "EffectInstance")
    public MobEffect attribute(MobEffect originalValue, Consumer<MobEffect> attributeModifier) {
        attributeModifier.accept(originalValue);
        return originalValue;
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
