package lib.kasuga.registration.minecraft_old.block;

import lib.kasuga.registration.core.ScopeHelper;
import lib.kasuga.registration.core.ModifierType;
import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class BlockReg<T extends Block> extends MinecraftDeferRegistryReg<BlockReg<T>, Block, T> implements BlockConfigurations<BlockReg<T>> {

    public static final ModifierType<Block> SCOPE = new ModifierType<>(true);

    private T value;
    private final Function<BlockReg<T>, Function<BlockBehaviour.Properties, T>> supplier;

    public static <T extends Block> BlockReg<T> of(String name, Function<BlockBehaviour.Properties, T> supplier) {
        return new BlockReg<T>(name, (i)->supplier);
    }

    public BlockReg(String name, Function<BlockReg<T>, Function<BlockBehaviour.Properties, T>> supplier) {
        super(name, Registries.BLOCK);
        this.supplier = supplier;
        this.configure(ScopeHelper.effect(SCOPE, this::getEntry));
    }

    @Override
    protected T createObject(ResourceLocation id) {
        BlockBehaviour.Properties properties = transform(BlockModifiers.TYPE_BLOCK_PROPERTIES, BlockBehaviour.Properties.of());
        return supplier.apply(this).apply(properties);
    }
}
