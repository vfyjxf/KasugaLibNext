package lib.kasuga.registration;

import lib.kasuga.KasugaLib;
import lib.kasuga.registration.core.RegisterContext;
import lib.kasuga.registration.core.RegisterContextRegistry;
import lib.kasuga.registration.core.ResourceLocationModifiers;
import lib.kasuga.registration.stages.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class Registry extends RegistryGroup {

    public Registry(String modId){
        this.configure(ResourceLocationModifiers.withNamespace(modId));
    }

    public void register(IEventBus modEventBus) {
        KasugaLib.afterRunning(modEventBus, ()->{
            KasugaLib.getBean(RegisterContextRegistry.class).configure(RegisterContextRegistry.Side.COMMON, this, modEventBus);
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void registerClient(IEventBus modEventBus) {
        KasugaLib.afterRunning(modEventBus, ()-> {
            KasugaLib.getBean(RegisterContextRegistry.class).configure(RegisterContextRegistry.Side.CLIENT, this, modEventBus);
        });
    }

    //    public void register(IEventBus modEventBus) {
//        modEventBus.addListener(EventPriority.HIGHEST, this::onRegisterEvent);
//        modEventBus.addListener(this::onRegisterNetworkPayload);
//        modEventBus.addListener(this::onRegisterCapabilities);
//        modEventBus.addListener(this::onBuildCreativeTabs);
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    public void registerClient(IEventBus modEventBus) {
//        modEventBus.addListener(this::onRegisterMenuScreensEvent);
//        modEventBus.addListener(this::onModelBakingComplete);
//    }
//
//    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
//        this.dispatchRegister(new RegisterContext<CapabilityRegistration>(
//                RegistrationStage.REGISTER_CAPABILITIES,
//                new CapabilityRegistration(event)
//        ));
//    }
//
//    private void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
//        RegisterContext<CreativeTabContentRegistration> context = new RegisterContext<>(
//                RegistrationStage.CREATIVE_TAB_CONTENT_REGISTRATION,
//                new CreativeTabContentRegistration(event.getTabKey(), event.getTab(), event)
//        );
//        this.dispatchRegister(context);
//    }
//
//    private void onRegisterNetworkPayload(RegisterPayloadHandlersEvent event) {
//        RegisterContext<PayloadRegistrationStage> context = new RegisterContext<>(
//                RegistrationStage.PAYLOAD_REGISTRATION,
//                new PayloadRegistrationStage(event::registrar)
//        );
//        this.dispatchRegister(context);
//    }
//
//    private void onRegisterEvent(RegisterEvent event) {
//        RegisterContext<RegisterStageContext> context = new RegisterContext<>(
//                RegistrationStage.REGISTER_EVENT,
//                new RegisterStageContext(event)
//        );
//        this.dispatchRegister(context);
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    private void onRegisterMenuScreensEvent(RegisterMenuScreensEvent event) {
//        RegisterContext<MenuScreenBindingStage> context = new RegisterContext<>(
//                RegistrationStage.MENU_SCREEN_BINDING,
//                new MenuScreenBindingStage.Instance(event)
//        );
//        this.dispatchRegister(context);
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    public void onModelBakingComplete(ModelEvent.BakingCompleted event) {
//        RegisterContext<BakingCompleteStage> context = new RegisterContext<>(
//                RegistrationStage.BAKING_COMPLETE,
//                new BakingCompleteStage()
//        );
//        this.dispatchRegister(context);
//    }
}
