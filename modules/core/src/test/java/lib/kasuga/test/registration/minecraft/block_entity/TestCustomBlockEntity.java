package lib.kasuga.test.registration.minecraft.block_entity;

import lib.kasuga.test.registration.minecraft.block_entity.BlockEntityRegistryTest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TestCustomBlockEntity extends BlockEntity {
    public TestCustomBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistryTest.TEST_CUSTOM_BLOCK_ENTITY.getEntry(), pos, blockState);
    }
}
