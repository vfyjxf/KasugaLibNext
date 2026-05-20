package lib.kasuga.registration.data_driven;

import com.mojang.logging.LogUtils;
import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import lib.kasuga.registration.core.RegisterContextRegistry;
import org.slf4j.Logger;

@Context
public class JsonTreeIntegration {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject
    private RegisterContextRegistry registry;

    @PostConstruct
    public void init() {
        // Build per-mod JSON tree lazily on first Registry dispatch,
        // after all @Context static initializers have registered factory types.
        registry.register(RegisterContextRegistry.Side.COMMON, (modRegistry, eventBus) -> {
            try {
                JsonRegistryGroup jsonRoot = JsonTreeBuilder.buildForMod(modRegistry.getModId());
                if (jsonRoot != null) {
                    modRegistry.addChild(jsonRoot);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to build JSON tree for mod '{}'", modRegistry.getModId(), e);
            }
        });
    }
}
