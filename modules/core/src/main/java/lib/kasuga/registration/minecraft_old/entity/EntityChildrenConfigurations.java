package lib.kasuga.registration.minecraft_old.entity;

import lib.kasuga.registration.core.IChildrenConfiguration;
import lib.kasuga.registration.core.IModifierConfigure;
import lib.kasuga.registration.minecraft_old.entity.attribute.EntityAttributeReg;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.Collections;
import java.util.function.Supplier;

public interface EntityChildrenConfigurations<S> extends IChildrenConfiguration<S>, IModifierConfigure<S> {
    public default S withAttribute(String name, Supplier<Attribute> attributeSupplier) {
        lib.kasuga.registration.minecraft_old.entity.attribute.EntityAttributeReg<?> attributeReg = new EntityAttributeReg<>(name, attributeSupplier);
        addChild(attributeReg);
        configure(EntityModifiers.ATTRIBUTES.apply(()-> Collections.singletonList(attributeReg.getHolder())));
        return self();
    }
}
