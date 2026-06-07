package test.kasuga.core.data_driven;

import io.micronaut.context.annotation.Context;
import lib.kasuga.registration.factory.FactoryRegistry;
import lib.kasuga.registration.minecraft.block.BlockReg;
import test.kasuga.core.data_driven.block.OccludingTestBlock;
import test.kasuga.core.data_driven.block.SimpleTestBlock;
import test.kasuga.core.data_driven.block.SlabTestBlock;

@Context
public class DataDrivenTestFactories {
    static {
        FactoryRegistry.register("simple_block", id -> BlockReg.of(id, SimpleTestBlock::new));
        FactoryRegistry.register("slab_block", id -> BlockReg.of(id, SlabTestBlock::new));
        FactoryRegistry.register("occluding_block", id -> BlockReg.of(id, OccludingTestBlock::new));
    }
}
