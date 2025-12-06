package lib.kasuga.registration.minecraft_old.entity;

import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.core.ModifierType;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

/**
 * Modifiers for entity registration configuration.
 */
public class EntityModifiers {
    
    public static final ModifierType<EntityType.Builder<?>> TYPE_ENTITY_BUILDER = new ModifierType<>();
    public static final ModifierType<Float> TYPE_WIDTH = new ModifierType<>();
    public static final ModifierType<Float> TYPE_HEIGHT = new ModifierType<>();
    public static final ModifierType<MobCategory> TYPE_CATEGORY = new ModifierType<>();

    public static final ModifierType<AttributeSupplier.Builder> TYPE_ATTRIBUTES = new ModifierType<>();

    public static abstract class SetEntityBuilderModifier extends Modifier<EntityType.Builder<?>> {
        @Override
        public ModifierType<EntityType.Builder<?>> getType() {
            return TYPE_ENTITY_BUILDER;
        }

        public static SetEntityBuilderModifier of(String name, Consumer<EntityType.Builder<?>> setter) {
            return new SetEntityBuilderModifier() {
                @Override
                public EntityType.Builder<?> transform(EntityType.Builder<?> originalValue) {
                    setter.accept(originalValue);
                    return originalValue;
                }
            };
        }
    }

    public static abstract class SetCategoryModifier extends Modifier<MobCategory> {
        @Override
        public ModifierType<MobCategory> getType() {
            return TYPE_CATEGORY;
        }

        public static SetCategoryModifier of(MobCategory category) {
            return new SetCategoryModifier() {
                @Override
                public MobCategory transform(MobCategory originalValue) {
                    return category;
                }
            };
        }
    }

    public static abstract class SetWidthModifier extends Modifier<Float> {
        @Override
        public ModifierType<Float> getType() {
            return TYPE_WIDTH;
        }

        public static SetWidthModifier of(float width) {
            return new SetWidthModifier() {
                @Override
                public Float transform(Float originalValue) {
                    return width;
                }
            };
        }
    }

    public static abstract class SetHeightModifier extends Modifier<Float> {
        @Override
        public ModifierType<Float> getType() {
            return TYPE_HEIGHT;
        }

        public static SetHeightModifier of(float height) {
            return new SetHeightModifier() {
                @Override
                public Float transform(Float originalValue) {
                    return height;
                }
            };
        }
    }

    public static abstract class AttributeModifier extends Modifier<AttributeSupplier.Builder> {
        @Override
        public ModifierType<AttributeSupplier.Builder> getType() {
            return TYPE_ATTRIBUTES;
        }
    }

    // Size modifiers
    public static Function<Float, Modifier<Float>> WIDTH =
            Util.memoize(SetWidthModifier::of);

    public static Function<Float, Modifier<Float>> HEIGHT =
            Util.memoize(SetHeightModifier::of);

    // Fire immunity
    public static Modifier<EntityType.Builder<?>> FIRE_IMMUNE =
            SetEntityBuilderModifier.of("fireImmune", EntityType.Builder::fireImmune);

    // No save
    public static Modifier<EntityType.Builder<?>> NO_SAVE =
            SetEntityBuilderModifier.of("noSave", EntityType.Builder::noSave);

    // No summon
    public static Modifier<EntityType.Builder<?>> NO_SUMMON =
            SetEntityBuilderModifier.of("noSummon", EntityType.Builder::noSummon);

    // Client tracking range
    public static Function<Integer, Modifier<EntityType.Builder<?>>> CLIENT_TRACKING_RANGE =
            Util.memoize((range) -> SetEntityBuilderModifier.of("clientTrackingRange", 
                    builder -> builder.clientTrackingRange(range)));

    // Update interval
    public static Function<Integer, Modifier<EntityType.Builder<?>>> UPDATE_INTERVAL =
            Util.memoize((interval) -> SetEntityBuilderModifier.of("updateInterval", 
                    builder -> builder.updateInterval(interval)));

    // Can spawn far from player
    public static Modifier<EntityType.Builder<?>> CAN_SPAWN_FAR_FROM_PLAYER =
            SetEntityBuilderModifier.of("canSpawnFarFromPlayer", EntityType.Builder::canSpawnFarFromPlayer);

    // Should receive velocity updates
    public static Modifier<EntityType.Builder<?>> SHOULD_RECEIVE_VELOCITY_UPDATES =
            SetEntityBuilderModifier.of("setShouldReceiveVelocityUpdates", 
                    builder -> builder.setShouldReceiveVelocityUpdates(true));

    public static Modifier<EntityType.Builder<?>> SHOULD_NOT_RECEIVE_VELOCITY_UPDATES =
            SetEntityBuilderModifier.of("setShouldNotReceiveVelocityUpdates", 
                    builder -> builder.setShouldReceiveVelocityUpdates(false));

    public static Function<MobCategory, Modifier<MobCategory>> CATEGORY =
            Util.memoize(SetCategoryModifier::of);

    public static Function<Supplier<Collection<Holder<Attribute>>>, Modifier<AttributeSupplier.Builder>> ATTRIBUTES =
            Util.memoize((attrs) -> new AttributeModifier() {
                @Override
                public AttributeSupplier.Builder transform(AttributeSupplier.Builder originalValue) {
                    for (Holder<Attribute> attr : attrs.get()) {
                        originalValue = originalValue.add(attr);
                    }
                    return originalValue;
                }
            });

    public static Function<Supplier<AttributeSupplier.Builder>, Modifier<AttributeSupplier.Builder>> ATTRIBUTES_MERGE =
            (attrSupplier) -> new AttributeModifier() {
                @Override
                public AttributeSupplier.Builder transform(AttributeSupplier.Builder originalValue) {
                    originalValue.combine(attrSupplier.get());
                    return originalValue;
                }
            };

}
