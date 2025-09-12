package lib.kasuga;

import jakarta.inject.Singleton;
import lib.kasuga.registration.Registry;
import lib.kasuga.registration.RegistryGroup;
import net.neoforged.fml.common.EventBusSubscriber;

public class KasugaLibRegistry {
    public static Registry getRegistryOf(String modId) {
        return new Registry(modId);
    }
}
