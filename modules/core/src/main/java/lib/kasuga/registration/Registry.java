package lib.kasuga.registration;

import lib.kasuga.registration.core.RegisterContext;
import lib.kasuga.registration.core.ResourceLocationModifiers;
import lib.kasuga.registration.stages.PayloadRegistrationStage;
import lib.kasuga.registration.stages.RegisterStageContext;
import lib.kasuga.registration.stages.RegistrationStage;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class Registry extends RegistryGroup {
    public Registry(String modId){
        this.configure(ResourceLocationModifiers.withNamespace(modId));
    }

    public void register(IEventBus modEventBus) {
        modEventBus.addListener(EventPriority.HIGHEST, this::onRegisterEvent);
        modEventBus.addListener(this::onRegisterNetworkPayload);
    }

    private void onRegisterNetworkPayload(RegisterPayloadHandlersEvent event) {
        RegisterContext<PayloadRegistrationStage> context = new RegisterContext<>(
                RegistrationStage.PAYLOAD_REGISTRATION,
                new PayloadRegistrationStage(event::registrar)
        );
        this.dispatchRegister(context);
    }

    private void onRegisterEvent(RegisterEvent event) {
        RegisterContext<RegisterStageContext> context = new RegisterContext<>(
                RegistrationStage.REGISTER_EVENT,
                new RegisterStageContext(event)
        );
        this.dispatchRegister(context);
    }

}
