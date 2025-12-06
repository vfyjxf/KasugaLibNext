package template.lib.kasuga.registration.minecraft.block;

import lib.kasuga.internal.generator.annotations.CodeTemplate;
import lib.kasuga.internal.generator.annotations.RegGenerator;
import lib.kasuga.internal.generator.facades.Inline;
import lib.kasuga.internal.generator.facades.RegFacade;
import lib.kasuga.registration.core.ModifierType;
import lib.kasuga.registration.core.ScopeHelper;
import lib.kasuga.registration.minecraft_old.block.ChildrenUtils;
import lib.kasuga.registration.minecraft_old.block_entity.BlockEntityModifiers;
import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import template.lib.kasuga.registration.minecraft.block_entity.BlockEntityReg;

import java.util.ArrayList;
import java.util.function.Function;

@CodeTemplate(generator = "Reg")
@RegGenerator(
        modifiers = {
                @RegGenerator.Modifier(
                        type="BlockProperties",
                        target = BlockBehaviour.Properties.class,
                        enumeration = {
                                "mapColor",
                                "noCollission",
                                "noOcclusion",
                                "friction",
                                "speedFactor",
                                "jumpFactor",
                                "sound",
                                "lightLevel",
                                "strength",
                                "randomTicks",
                                "dynamicShape",
                                "noLootTable",
                                "dropsLike",
                                "lootFrom",
                                "ignitedByLava",
                                "liquid",
                                "forceSolidOn",
                                "pushReaction",
                                "air",
                                "isValidSpawn",
                                "isRedstoneConductor",
                                "isSuffocating",
                                "isViewBlocking",
                                "hasPostProcess",
                                "emissiveRendering",
                                "requiresCorrectToolForDrops",
                                "destroyTime",
                                "explosionResistance",
                                "offsetType",
                                "noTerrainParticles",
                                "requiredFeatures",
                                "instrument",
                                "replaceable"
                        }
                )
        }
)
public class BlockReg<T extends Block> extends MinecraftDeferRegistryReg<BlockReg<T>, Block, T> implements Inline<Test> {
    public static final ModifierType<Block> SCOPE = new ModifierType<>(true);

    private final Function<BlockReg<T>, Function<BlockBehaviour.Properties, T>> supplier;


    public BlockReg(String name, @RegGenerator.SelfReference() Function<BlockReg<T>, Function<BlockBehaviour.Properties, T>> supplier) {
        super(name, Registries.BLOCK);
        this.supplier = supplier;
        this.configure(ScopeHelper.effect(SCOPE, this::getEntry));
    }

    @RegGenerator.ChildrenConfiguration(target = BlockEntityReg.class)
    public void withBlockEntity(BlockEntityReg<?> reg){
        reg.configure(BlockEntityModifiers.VALID_BLOCK_BY_SUPPLIER.apply(()-> {
            ArrayList<BlockReg<?>> l = new ArrayList<BlockReg<?>>();
            ChildrenUtils.traverse(this, (r)->{
                if(r instanceof BlockReg<?> br) {
                    l.add(br);
                }
            });
            return l.stream().map(i->(Block) i.getEntry()).toList();
        }));
    }

    @Override
    protected T createObject(ResourceLocation id) {
        BlockBehaviour.Properties properties = RegFacade.transformObject("BlockProperties", BlockBehaviour.Properties.of());
        return supplier.apply(this).apply(properties);
    }
}
