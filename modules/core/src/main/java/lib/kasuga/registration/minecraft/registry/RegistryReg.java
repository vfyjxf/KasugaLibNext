package lib.kasuga.registration.minecraft.registry;

import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.RegisterContext;
import lib.kasuga.registration.stages.RegistrationStage;
import net.minecraft.core.Registry;

public class RegistryReg<T> extends Reg<RegistryReg<T>, Registry<T>> {
    Registry<T> registry;

    public RegistryReg(Registry<T> registry) {
        this.registry = registry;
    }

    @Override
    public Registry<T> getEntry() {
        return registry;
    }

    @Override
    public void register(RegisterContext<?> context) {
        context.onStage(RegistrationStage.NEW_REGISTRY, event->{
            event.register(registry);
        });
    }
}
