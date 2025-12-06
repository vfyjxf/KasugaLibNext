package test.kasuga.core;

import com.mojang.logging.LogUtils;
import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibRegistry;
import lib.kasuga.registration.Registry;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;

@Context()
public class CoreTestApplication {
    public static Registry registry = KasugaLibRegistry.getRegistryOf(KasugaLib.MODID);
    private Logger logger = LogUtils.getLogger();

    @Inject() @Named("modEventBus") IEventBus eventBus;

    @PostConstruct()
    public void init() {
        registry.register(eventBus);
    }
}
