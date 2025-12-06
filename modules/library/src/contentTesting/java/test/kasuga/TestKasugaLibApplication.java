package test.kasuga;

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
public class TestKasugaLibApplication {
    private Logger logger = LogUtils.getLogger();
    public static Registry REGISTRY = KasugaLibRegistry.getRegistryOf(KasugaLib.MODID);

    @Inject() @Named("modEventBus")
    IEventBus eventBus;

    @PostConstruct()
    public void init() {
        REGISTRY.register(eventBus);
        logger.info("KasugaLib Test Application Context initialized.");
    }
}
