package lib.kasuga;

import com.mojang.logging.LogUtils;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lib.kasuga.inject.class_loader.BeanOnlyIn;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;

@Singleton
@BeanOnlyIn.Client
public class KasugaLibClientApplication {
    @Inject()
    ModContainer modContainer;

    Logger logger = LogUtils.getLogger();
    @PostConstruct
    public void init() {
        // Initialize the application
        logger.info("KasugaLibApplication Client initialized.");
        logger.info("Application ID:" + modContainer.getModId());
        logger.info("Minecraft object:" + Minecraft.getInstance());
    }
}
