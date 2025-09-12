package lib.kasuga.registration.minecraft.entity;

import lib.kasuga.registration.core.IModifierConfigure;
import lib.kasuga.registration.minecraft.entity.attribute.EntityAttributeReg;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * Configuration interface for entity registration.
 * This interface provides common configuration methods for entities.
 * 
 * @param <S> the type of the entity registration
 */
public interface EntityConfigurations<S extends EntityConfigurations<S>> extends IModifierConfigure<S> {
    
    /**
     * Set the size of the entity.
     * @param width the width of the entity
     * @param height the height of the entity
     * @return self
     */
    default S size(float width, float height) {
        return configure(EntityModifiers.WIDTH.apply(width))
                     .configure(EntityModifiers.HEIGHT.apply(height));
    }

    /**
     * Make the entity fire immune.
     * @return self
     */
    default S fireImmune() {
        return configure(EntityModifiers.FIRE_IMMUNE);
    }
    
    /**
     * Make the entity require no save.
     * @return self
     */
    default S noSave() {
        return configure(EntityModifiers.NO_SAVE);
    }

    /**
     * Make the entity not summonable.
     * @return self
     */
    default S noSummon() {
        return configure(EntityModifiers.NO_SUMMON);
    }

    /**
     * Set the client tracking range for the entity.
     * @param range the tracking range
     * @return self
     */
    default S clientTrackingRange(int range) {
        return configure(EntityModifiers.CLIENT_TRACKING_RANGE.apply(range));
    }

    /**
     * Set the update interval for the entity.
     * @param interval the update interval
     * @return self
     */
    default S updateInterval(int interval) {
        return configure(EntityModifiers.UPDATE_INTERVAL.apply(interval));
    }

    default S category(MobCategory category) {
        return category(()->category);
    }

    default S category(Supplier<MobCategory> category) {
        return configure(EntityModifiers.SetCategoryModifier.of(category.get()));
    }

    default S withAttribute(EntityAttributeReg<?> attribute) {
        return withAttributeSupplier(()-> Collections.singletonList(attribute.getHolder()));
    }

    default S withAttributeSupplier(Supplier<Collection<Holder<Attribute>>> attribute) {
        return configure(EntityModifiers.ATTRIBUTES.apply(attribute));
    }
}
