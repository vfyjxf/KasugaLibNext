package template.lib.kasuga.registration.minecraft.entity;

import lib.kasuga.internal.generator.annotations.CodeTemplate;
import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.function.Supplier;

@CodeTemplate(generator = "Reg")
public class EntityAttributeReg<T extends Attribute> extends MinecraftDeferRegistryReg<EntityAttributeReg<T>, Attribute, T> {
    private final Supplier<T> supplier;

    public EntityAttributeReg(String name, Supplier<T> supplier) {
        super(name, Registries.ATTRIBUTE);
        this.supplier = supplier;
    }

    @Override
    protected T createObject(ResourceLocation id) {
        return supplier.get();
    }
}
