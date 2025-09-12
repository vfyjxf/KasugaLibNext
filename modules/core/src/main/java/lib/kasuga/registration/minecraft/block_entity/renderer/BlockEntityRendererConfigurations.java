package lib.kasuga.registration.minecraft.block_entity.renderer;

import lib.kasuga.registration.core.IModifierConfigure;
import lib.kasuga.registration.minecraft.block_entity.BlockEntityReg;

import java.util.List;

public interface BlockEntityRendererConfigurations<S extends BlockEntityRendererConfigurations<S>> extends IModifierConfigure<S> {
    public default S withBlockEntity(BlockEntityReg<?> blockEntityReg) {
        return configure(BlockEntityRendererModifiers.BLOCK_ENTITY_BY_SUPPLIER.apply(()-> List.of(blockEntityReg.getEntry())));
    }
}
