package lib.kasuga;

import com.mojang.logging.LogUtils;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.inject.BeanDefinitionReference;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lib.kasuga.core.resource.ResourceSystem;
import lib.kasuga.core.resource.ScopedResourceManager;
import lib.kasuga.registration.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;

import java.net.URL;
import java.util.Optional;

@Context()
public class KasugaLibApplication {
    @Inject() ModContainer modContainer;

    @Inject()
    Optional<KasugaLibClientApplication> client;

    @Inject() @Named("modEventBus")
    IEventBus modEventBus;

    public static final Registry REGISTRY = KasugaLibRegistry.getRegistryOf(KasugaLib.MODID);
    Logger logger = LogUtils.getLogger();
    @PostConstruct
    public void init() {
        // Initialize the application
        logger.info("KasugaLibApplication initialized.");
        logger.info("Application ID:" + modContainer.getModId());
        logger.info("ClientMode: " + String.valueOf(client.isPresent()));
        logger.info("Context Class:" + ApplicationContext.class.getName());
        REGISTRY.register(modEventBus);
    }
}
