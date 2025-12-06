package lib.kasuga.registration.minecraft_old.block_entity.renderer;

import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.core.ModifierType;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockEntityRendererModifiers {
    public static final ModifierType<Collection<BlockEntityType<?>>> BLOCK_ENTITIES = new ModifierType<>();

    // 内部类：可复用的 BlockEntityType 列表 Modifier
    public static class BlockEntityListModifier extends Modifier<Collection<BlockEntityType<?>>> {
        @Override
        public ModifierType<Collection<BlockEntityType<?>>> getType() {
            return BLOCK_ENTITIES;
        }
    }

    public static final Function<Supplier<Collection<BlockEntityType<?>>>, Modifier<Collection<BlockEntityType<?>>>>
            BLOCK_ENTITY_BY_SUPPLIER = Util.memoize((supplier) -> new BlockEntityListModifier() {
        @Override
        public Collection<BlockEntityType<?>> transform(Collection<BlockEntityType<?>> originalValue) {
            originalValue.addAll(supplier.get());
            return originalValue;
        }
    });


    public static final Function<BiPredicate<ResourceLocation, BlockEntityType<?>>, Modifier<Collection<BlockEntityType<?>>>>
            BLOCK_ENTITIES_BY_PREDICATE = (predicate) -> new BlockEntityListModifier() {
        @Override
        public Collection<BlockEntityType<?>> transform(Collection<BlockEntityType<?>> originalValue) {
            LinkedList<BlockEntityType<?>> matchedList = new LinkedList<>();
            for (Map.Entry<ResourceKey<BlockEntityType<?>>, BlockEntityType<?>> entry : BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet()) {
                if (predicate.test(entry.getKey().location(), entry.getValue())) {
                    matchedList.add(entry.getValue());
                }
            }
            originalValue.addAll(matchedList);
            return originalValue;
        }
    };
}
