package lib.kasuga.registration.minecraft.block_entity;

import lib.kasuga.registration.core.ScopeHelper;
import lib.kasuga.registration.core.ModifierType;
import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class BlockEntityReg<T extends BlockEntity> extends MinecraftDeferRegistryReg<BlockEntityReg<T>, BlockEntityType<?>, BlockEntityType<T>> implements BlockEntityConfigurations<BlockEntityReg<T>> {

    public static final ModifierType<BlockEntityType<? extends BlockEntity>> SCOPE = new ModifierType<>(true);

    private final Function<BlockEntityReg<T>, BlockEntityType.BlockEntitySupplier<T>> supplier;
    private BlockEntityType<T> value;

    public static <T extends BlockEntity> BlockEntityReg<T> of(String name, BlockEntityType.BlockEntitySupplier<T> supplier) {
        return new BlockEntityReg<T>(name, i -> supplier);
    }

    public BlockEntityReg(String name, Function<BlockEntityReg<T>, BlockEntityType.BlockEntitySupplier<T>> supplier) {
        super(name, Registries.BLOCK_ENTITY_TYPE);
        this.supplier = supplier;
        configure(ScopeHelper.effect(SCOPE, this::getEntry));
    }

    @Override
    protected BlockEntityType<T> createObject(ResourceLocation id) {
        Collection<Block> validBlocks = transform(BlockEntityModifiers.VALID_BLOCKS_TYPE, new ArrayList<>());
        com.mojang.datafixers.types.Type<?> dataType = transform(BlockEntityModifiers.DATA_TYPES, null);
        Block[] blocks = validBlocks.toArray(new Block[0]);
        return BlockEntityType.Builder.of(supplier.apply(this), blocks).build(dataType);
    }
}
