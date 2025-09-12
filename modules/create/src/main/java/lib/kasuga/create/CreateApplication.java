package lib.kasuga.create;

import com.mojang.logging.LogUtils;
import com.simibubi.create.Create;
import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;

@Context
@CreateModBeans()
public class CreateApplication {
    Logger logger = LogUtils.getLogger();
    @PostConstruct
    public void init() {
        // Initialize the application
        logger.info("KasugaLibApplicationCreate initialized.");
        logger.info("Create ID:" + Create.ID);
        logger.info("Create Name:" + Create.NAME);
    }
}
