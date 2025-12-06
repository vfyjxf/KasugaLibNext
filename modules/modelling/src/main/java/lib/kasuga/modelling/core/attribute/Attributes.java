package lib.kasuga.modelling.core.attribute;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class Attributes {
    public static ResourceKey<Registry<AttributeType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("kasugalib", "modelling/attribute"));
    public static Registry<AttributeType<?>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY)
            .create();
}
