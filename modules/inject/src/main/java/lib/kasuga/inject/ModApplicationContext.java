package lib.kasuga.inject;

import io.micronaut.context.*;
import io.micronaut.inject.qualifiers.Qualifiers;
import lib.kasuga.inject.auto_configure.Configurable;
import lib.kasuga.inject.class_loader.CombinedClassLoader;
import lib.kasuga.inject.class_loader.Envs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

public class ModApplicationContext {
    public static ApplicationContext create(Class<?> modMainClass) {
        if(Envs.isDevEnvironment()) {
            return ApplicationContext.builder(new CombinedClassLoader(
                    modMainClass.getClassLoader(),
                    ApplicationContext.class.getClassLoader()
            )).build();
        }
        return ApplicationContext.builder(modMainClass.getClassLoader()).build();
    }

    public static void init(ApplicationContext context, IEventBus modEventBus, ModContainer modContainer) {
        context.registerSingleton(IEventBus.class, modEventBus, Qualifiers.byName("modEventBus"));
        context.registerSingleton(IEventBus.class, NeoForge.EVENT_BUS, Qualifiers.byName("forgeEventBus"));
        context.registerSingleton(ModContainer.class, modContainer, Qualifiers.byName("modContainer"));
        context.start();
        Configurable.configure(context, modEventBus, modContainer);
    }
}
