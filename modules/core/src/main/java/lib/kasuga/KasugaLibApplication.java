package lib.kasuga;

import com.mojang.logging.LogUtils;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;

import java.util.Optional;

@Singleton()
public class KasugaLibApplication {
    @Inject() ModContainer modContainer;

    @Inject()
    Optional<KasugaLibClientApplication> client;
    Logger logger = LogUtils.getLogger();
    @PostConstruct
    public void init() {
        // Initialize the application
        logger.info("KasugaLibApplication initialized.");
        logger.info("Application ID:" + modContainer.getModId());
        logger.info("ClientMode: " + String.valueOf(client.isPresent()));
    }
}
