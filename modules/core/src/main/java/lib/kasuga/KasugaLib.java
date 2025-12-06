package lib.kasuga;

import io.micronaut.context.ApplicationContext;

import lib.kasuga.content.document.DocumentComponentRegistries;
import lib.kasuga.content.document.DocumentItem;
import lib.kasuga.inject.ModApplicationContext;
import lib.kasuga.early.ModLoadingManager;
import lib.kasuga.early.ModLoadingProgress;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(KasugaLib.MODID)
public class KasugaLib {
    public static final ApplicationContext context = ModApplicationContext.create(KasugaLib.class);
    public static final String MODID = "kasuga_lib";

    public KasugaLib(IEventBus modEventBus, ModContainer modContainer) {
        ModApplicationContext.init(context, modEventBus, modContainer);
        modEventBus.addListener(DocumentComponentRegistries::onRegistryEvent);
        DocumentItem.REGISTRAR.register(modEventBus);
    }

    public static ApplicationContext getContext() {
        return context;
    }
}
