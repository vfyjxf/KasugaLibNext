package lib.kasuga.test.registration.minecraft.block_entity;

import lib.kasuga.test.registration.minecraft.block.BlockRegistryTest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TestBlockEntity extends BlockEntity {
    public TestBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockRegistryTest.TEST_BLOCK.getBlockEntityType("test_block_entity"), pos, blockState);
    }
}
