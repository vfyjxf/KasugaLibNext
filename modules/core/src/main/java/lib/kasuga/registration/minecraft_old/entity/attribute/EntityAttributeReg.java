package lib.kasuga.registration.minecraft_old.entity.attribute;

import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.function.Supplier;

public class EntityAttributeReg<T extends Attribute> extends MinecraftDeferRegistryReg<EntityAttributeReg<T>, Attribute, T> {
    Supplier<T> supplier;

    public EntityAttributeReg(String name, Supplier<T> supplier) {
        super(name, Registries.ATTRIBUTE);
    }
    @Override
    protected T createObject(ResourceLocation id) {
        return supplier.get();
    }
}
