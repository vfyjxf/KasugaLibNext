package lib.kasuga.registration;

import lib.kasuga.registration.core.RegisterContext;
import lib.kasuga.registration.core.ResourceLocationModifiers;
import lib.kasuga.registration.stages.RegisterStageContext;
import lib.kasuga.registration.stages.RegistrationStage;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class Registry extends RegistryGroup {
    public Registry(String modId){
        this.configure(ResourceLocationModifiers.withNamespace(modId));
    }

    public void register(IEventBus eventBus) {
        eventBus.addListener(EventPriority.HIGHEST, this::onRegisterEvent);
    }

    private void onRegisterEvent(RegisterEvent event) {
        RegisterContext<RegisterStageContext> context = new RegisterContext<>(RegistrationStage.REGISTER_EVENT,new RegisterStageContext(event));
        this.dispatchRegister(context);
    }

}
