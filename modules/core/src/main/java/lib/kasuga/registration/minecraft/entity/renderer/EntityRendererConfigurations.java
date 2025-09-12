package lib.kasuga.registration.minecraft.entity.renderer;

import lib.kasuga.registration.core.IModifierConfigure;
import lib.kasuga.registration.minecraft.entity.EntityReg;

import java.util.List;

public interface EntityRendererConfigurations<S extends EntityRendererConfigurations<S>> extends IModifierConfigure<S> {
    default S withEntity(EntityReg<?> entityReg) {
        return configure(EntityRendererModifiers.ENTITY_BY_SUPPLIER.apply(() -> List.of(entityReg.getEntry())));
    }
}
