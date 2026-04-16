package lib.kasuga.rendering.models.mc.proxies;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class BlockProxy<ProxiedType extends Block, ProxiedInstanceType extends BlockState> implements ElementProxy<ProxiedType, ProxiedInstanceType> {

    private final ProxiedType block;

    public BlockProxy(ProxiedType block) {
        this.block = block;
    }

    @Override
    public boolean isValidInput(Object input) {
        return Objects.equals(input, block);
    }

    @Override
    public boolean isValidInstance(Object instance) {
        if (!(instance instanceof BlockState state)) return false;
        return state.is(block);
    }
}
