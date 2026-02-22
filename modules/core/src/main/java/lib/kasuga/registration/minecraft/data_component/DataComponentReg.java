package lib.kasuga.registration.minecraft.data_component;

import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import java.util.function.UnaryOperator;

public class DataComponentReg<T> extends MinecraftDeferRegistryReg<DataComponentReg<T>, DataComponentType<?>, DataComponentType<T>> {
    private final UnaryOperator<DataComponentType.Builder<T>> builder;

    public DataComponentReg(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        super(name, Registries.DATA_COMPONENT_TYPE);
        this.builder = builder;
    }

    @Override
    protected DataComponentType<T> createObject(ResourceLocation id) {
        return builder.apply(DataComponentType.builder()).build();
    }
}
