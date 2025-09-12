package lib.kasuga.registration.stages;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Supplier;

public class RegisterStageContext {
    private final RegisterEvent event;

    public RegisterStageContext(RegisterEvent event) {
        this.event = event;
    }

    public <T> void register(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation name, Supplier<T> valueSupplier) {
        event.register(registryKey, name, valueSupplier);
    }

    public ResourceKey<?> getRegistryKey() {
        return event.getRegistryKey();
    }


}
