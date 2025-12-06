package template.lib.kasuga.registration.minecraft.block_entity;

import com.mojang.datafixers.types.Type;
import lib.kasuga.internal.generator.annotations.CodeTemplate;
import lib.kasuga.internal.generator.annotations.RegGenerator;
import lib.kasuga.internal.generator.facades.RegFacade;
import lib.kasuga.registration.core.ModifierType;
import lib.kasuga.registration.core.ScopeHelper;
import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

@CodeTemplate(generator = "Reg")
@RegGenerator(modifiers = {
    @RegGenerator.Modifier(
            type = "ValidBlocksType",
            target = Collection.class,
            extendedType = "Collection<Block>"
    ),
    @RegGenerator.Modifier(
            type = "DataType",
            target = Type.class,
            extendedType = "com.mojang.datafixers.types.Type<?>"
    )
})
public class BlockEntityReg<T extends BlockEntity> extends MinecraftDeferRegistryReg<BlockEntityReg<T>, BlockEntityType<?>, BlockEntityType<T>>{

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

    @RegGenerator.ModifyFunction(type = "ValidBlocksType")
    public Collection<Block> validBlocks(Collection<Block> blocks, Collection<Supplier<Block>> addedBlocks) {
        for (Supplier<? extends Block> addedBlock : addedBlocks) {
            blocks.add(addedBlock.get());
        }
        return blocks;
    }

    @RegGenerator.ModifyFunction(type = "ValidBlocksType")
    public Collection<Block> validBlocks(Collection<Block> originalValue, BiPredicate<ResourceLocation, Block> blockPredicate) {
        LinkedList<Block> castBlockList = new LinkedList<>();
        for (Map.Entry<ResourceKey<Block>, Block> entries : BuiltInRegistries.BLOCK.entrySet()) {
            if (blockPredicate.test(entries.getKey().location(), entries.getValue()))
                castBlockList.add(entries.getValue());
        }
        originalValue.addAll(castBlockList);
        return originalValue;
    }


    @Override
    protected BlockEntityType<T> createObject(ResourceLocation id) {
        Collection<Block> validBlocks = RegFacade.transformObject("ValidBlocksType", new ArrayList<>());
        Type<?> dataType = RegFacade.transformObject("DataType", null);
        Block[] blocks = validBlocks.toArray(new Block[0]);
        return BlockEntityType.Builder.of(supplier.apply(this), blocks).build(dataType);
    }
}
