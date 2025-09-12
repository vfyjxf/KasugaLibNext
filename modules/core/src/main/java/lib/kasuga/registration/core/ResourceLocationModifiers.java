package lib.kasuga.registration.core;

import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.core.ModifierType;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class ResourceLocationModifiers {
    public static ModifierType<ResourceLocation> ID = new ModifierType<>();

    protected static Function<Function<ResourceLocation, ResourceLocation>, Modifier<ResourceLocation>> ID_FUNC = Util.memoize((i)->{
        return new Modifier<ResourceLocation>() {
            @Override
            public ModifierType<ResourceLocation> getType() {
                return ID;
            }

            @Override
            public ResourceLocation transform(ResourceLocation originalValue) {
                return i.apply(originalValue);
            }
        };
    });

    public static Modifier<ResourceLocation> id(Function<ResourceLocation, ResourceLocation> function) {
        return ID_FUNC.apply(function);
    }

    public static Modifier<ResourceLocation> withNamespace(String namespace) {
        return id((loc)->ResourceLocation.fromNamespaceAndPath(namespace, loc.getPath()));
    }
}
