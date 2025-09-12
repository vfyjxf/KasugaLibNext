package lib.kasuga.registration.minecraft.block_entity;

import com.machinezoo.noexception.CheckedExceptionHandler;
import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.core.ModifierType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockEntityModifiers {
    public static ModifierType<Collection<Block>> VALID_BLOCKS_TYPE = new ModifierType<>();

    public static ModifierType<com.mojang.datafixers.types.Type<?>> DATA_TYPES = new ModifierType<>();

    public static class ValidBlockListModifier extends Modifier<Collection<Block>> {
        @Override
        public ModifierType<Collection<Block>> getType() {
            return VALID_BLOCKS_TYPE;
        }
    }

    public static Function<Supplier<Collection<Block>>, Modifier<Collection<Block>>> VALID_BLOCK_BY_SUPPLIER =
        (blockSupplier) -> new ValidBlockListModifier() {
            @Override
            public Collection<Block> transform(Collection<Block> originalValue) {
                originalValue.addAll(blockSupplier.get());
                return originalValue;
            }
        };
    public static Function<BiPredicate<ResourceLocation, Block>, Modifier<Collection<Block>>> VALID_BLOCKS_BY_PREDICATE =
        (blockPredicate) -> new ValidBlockListModifier() {
            @Override
            public Collection<Block> transform(Collection<Block> originalValue) {
                LinkedList<Block> castBlockList = new LinkedList<>();
                for (Map.Entry<ResourceKey<Block>, Block> entries : BuiltInRegistries.BLOCK.entrySet()) {
                    if (blockPredicate.test(entries.getKey().location(), entries.getValue()))
                        castBlockList.add(entries.getValue());
                }
                originalValue.addAll(castBlockList);
                return originalValue;
            }
        };


}
