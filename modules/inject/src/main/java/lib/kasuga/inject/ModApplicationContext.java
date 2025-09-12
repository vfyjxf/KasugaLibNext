package lib.kasuga.inject;

import com.mojang.logging.LogUtils;
import io.micronaut.context.*;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.qualifiers.Qualifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModApplicationContext {
    public static ApplicationContext create(Class<?> modMainClass) {
        if(Envs.isDevEnvironment()) {
            return ApplicationContext.builder(new CombinedClassLoader(
                    modMainClass.getClassLoader(),
                    FMLLoader.class.getClassLoader()
            )).build();
        }
        return ApplicationContext.builder(modMainClass.getClassLoader()).build();
    }
}
