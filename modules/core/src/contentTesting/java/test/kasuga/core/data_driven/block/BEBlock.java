package test.kasuga.core.data_driven.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import test.kasuga.core.data_driven.block_entity.TestBlockEntity;

public class BEBlock extends SimpleTestBlock implements EntityBlock {

    public BEBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            if (type.isValid(state)) {
                return new TestBlockEntity(type, pos, state);
            }
        }
        return null;
    }
}
