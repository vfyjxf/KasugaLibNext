package lib.kasuga.registration.minecraft.entity.renderer;

import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.core.ModifierType;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntityRendererModifiers {
    public static final ModifierType<Collection<EntityType<?>>> ENTITY_TYPES = new ModifierType<>();

    public static class EntityTypeListModifier extends Modifier<Collection<EntityType<?>>> {
        @Override
        public ModifierType<Collection<EntityType<?>>> getType() {
            return ENTITY_TYPES;
        }
    }

    public static final Function<Supplier<Collection<EntityType<?>>>, Modifier<Collection<EntityType<?>>>>
            ENTITY_BY_SUPPLIER = Util.memoize((supplier) -> new EntityTypeListModifier() {
        @Override
        public Collection<EntityType<?>> transform(Collection<EntityType<?>> originalValue) {
            originalValue.addAll(supplier.get());
            return originalValue;
        }
    });

    public static final Function<BiPredicate<ResourceLocation, EntityType<?>>, Modifier<Collection<EntityType<?>>>>
            ENTITY_TYPES_BY_PREDICATE = (predicate) -> new EntityTypeListModifier() {
        @Override
        public Collection<EntityType<?>> transform(Collection<EntityType<?>> originalValue) {
            LinkedList<EntityType<?>> matchedList = new LinkedList<>();
            for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
                if (predicate.test(entry.getKey().location(), entry.getValue())) {
                    matchedList.add(entry.getValue());
                }
            }
            originalValue.addAll(matchedList);
            return originalValue;
        }
    };
}
