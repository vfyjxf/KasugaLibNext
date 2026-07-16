package lib.kasuga.registration.core;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class CreativeTabModifiers {
    public static final ModifierType<ResourceLocation> TYPE = new ModifierType<>();

    public static Modifier<ResourceLocation> set(Supplier<ResourceLocation> tabSupplier) {
        return new Modifier<ResourceLocation>() {
            @Override
            public ModifierType<ResourceLocation> getType() {
                return TYPE;
            }

            @Override
            public ResourceLocation transform(ResourceLocation originalValue) {
                return tabSupplier.get();
            }
        };
    }
}
