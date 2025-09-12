package lib.kasuga.registration.minecraft.entity;

import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;

import java.util.function.BiFunction;

/**
 * Entity is a basic element of minecraft. We use it to create mobs, minecarts and other movable creatures.
 * See {@link Entity} for more info.
 * @param <T> the class of your entity.
 */
public final class EntityReg<T extends Entity> extends MinecraftDeferRegistryReg<EntityReg<T>, EntityType<?>, EntityType<T>> 
        implements EntityConfigurations<EntityReg<T>>, EntityChildrenConfigurations<EntityReg<T>> {

    private final BiFunction<EntityType<?>, Level, T> entityFactory;

    public static <T extends Entity> EntityReg<T> of(String name, BiFunction<EntityType<?>, Level, T> factory) {
        return new EntityReg<>(name, factory);
    }

    public static <T extends Entity> EntityReg<T> of(String name, MobCategory category, BiFunction<EntityType<?>, Level, T> factory) {
        return new EntityReg<>(name, factory).category(category);
    }

    public EntityReg(String name, BiFunction<EntityType<?>, Level, T> entityFactory) {
        super(name, Registries.ENTITY_TYPE);
        this.entityFactory = entityFactory;
    }

    @Override
    protected EntityType<T> createObject(ResourceLocation id) {

        MobCategory mobCategory = transform(EntityModifiers.TYPE_CATEGORY, MobCategory.MISC);

        EntityType.Builder<T> builder = EntityType.Builder.of(entityFactory::apply, mobCategory);
        
        // Apply size modifier if present
        Float width = transform(EntityModifiers.TYPE_WIDTH, null);
        Float height = transform(EntityModifiers.TYPE_HEIGHT, null);
        if (width != null && height != null) {
            builder = builder.sized(width, height);
        }

        //noinspection unchecked
        return (EntityType<T>) transform(EntityModifiers.TYPE_ENTITY_BUILDER, builder).build(id.toString());
    }
}
