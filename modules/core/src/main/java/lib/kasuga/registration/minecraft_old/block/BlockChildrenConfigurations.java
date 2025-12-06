package lib.kasuga.registration.minecraft_old.block;

import lib.kasuga.registration.core.IChildrenConfiguration;
import lib.kasuga.registration.core.IModifierConfigure;
import lib.kasuga.registration.minecraft_old.block_entity.BlockEntityModifiers;
import lib.kasuga.registration.minecraft_old.block_entity.BlockEntityReg;
import lib.kasuga.registration.minecraft_old.item.ItemReg;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface BlockChildrenConfigurations<S> extends IChildrenConfiguration<S>, IModifierConfigure<S> {
    public default <T extends BlockEntity> S withBlockEntity(String name, BlockEntityType.BlockEntitySupplier<T> supplier) {
        BlockEntityReg<T> blockEntityReg = BlockEntityReg.of(name,supplier);
        blockEntityReg.configure(BlockEntityModifiers.VALID_BLOCK_BY_SUPPLIER.apply(()-> {
            ArrayList<lib.kasuga.registration.minecraft_old.block.BlockReg<?>> l = new ArrayList<lib.kasuga.registration.minecraft_old.block.BlockReg<?>>();
            ChildrenUtils.traverse(this, (r)->{
                if(r instanceof lib.kasuga.registration.minecraft_old.block.BlockReg<?> br) {
                    l.add(br);
                }
            });
            return l.stream().map(i->(Block) i.getEntry()).toList();
        }));
        return addChild(blockEntityReg);
    }

    public default BlockEntityType<?> getBlockEntityType(){
        return ChildrenUtils.traverseRI(this, r->r instanceof BlockEntityReg<?> ber ? ber.getEntry() : null);
    }

    public default BlockEntityType<?> getBlockEntityType(String id){
        return ChildrenUtils.traverseRI(this, r->r instanceof BlockEntityReg<?> ber && ber.getName().equals(id) ? ber.getEntry() : null);
    }

    public default S withDefaultBlockItem(String name) {
        ItemReg<BlockItem> itemReg = new ItemReg<>(name, i->p->new BlockItem(i.transform(lib.kasuga.registration.minecraft_old.block.BlockReg.SCOPE, null), p));
        return addChild(itemReg);
    }

    public default S withBlockItem(String name, Supplier<BiFunction<Block, Item.Properties, BlockItem>> supplier) {
        ItemReg<BlockItem> itemReg = new ItemReg<>(name, i->p->supplier.get().apply(i.transform(BlockReg.SCOPE, null), p));
        return addChild(itemReg);
    }
}
