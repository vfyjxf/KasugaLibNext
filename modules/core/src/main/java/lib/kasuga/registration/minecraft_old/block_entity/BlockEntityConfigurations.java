package lib.kasuga.registration.minecraft_old.block_entity;

import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.IModifierConfigure;
import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.minecraft_old.block.BlockSupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public interface BlockEntityConfigurations<S extends BlockEntityConfigurations<S>> extends IModifierConfigure<S> {

    static abstract class ConsumeAdapter implements BlockEntityConfigurations<BlockEntityConfigurations.ConsumeAdapter> { }

    public static <T extends Reg<T, ?>> Consumer<T> adaptConsume(Consumer<ConsumeAdapter> s){
        return (i)->s.accept(new ConsumeAdapter(){
            @Override
            public ConsumeAdapter configure(Modifier<?> modifier) {
                i.configure(modifier);
                return this;
            }
        });
    }

    public default S validBlocks(BlockSupplier... suppliers) {
        return configure(BlockEntityModifiers.VALID_BLOCK_BY_SUPPLIER.apply(()-> {
            ArrayList<Block> blocks = new ArrayList<>();
            for (BlockSupplier supplier : suppliers) {
                blocks.add(supplier.get());
            }
            return blocks;
        }));
    }

    public default S validBlocks(BiPredicate<ResourceLocation, Block> predicate) {
        return configure(BlockEntityModifiers.VALID_BLOCKS_BY_PREDICATE.apply(predicate));
    }
}
