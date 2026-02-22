package lib.kasuga.inject;

import io.micronaut.context.*;
import io.micronaut.inject.qualifiers.Qualifiers;
import lib.kasuga.inject.auto_configure.Configurable;
import lib.kasuga.inject.class_loader.CombinedClassLoader;
import lib.kasuga.inject.class_loader.Envs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforgespi.language.IModInfo;

public class ModApplicationContext {
    public static ApplicationContext create(Class<?> modMainClass) {
        if(Envs.isDevEnvironment()) {
            ClassLoader classLoader = new CombinedClassLoader(
                    modMainClass.getClassLoader(),
                    ApplicationContext.class.getClassLoader()
            );
            return ApplicationContext.builder().mainClass(modMainClass).classLoader(classLoader).beanResolutionTrace(BeanResolutionTraceMode.STANDARD_OUT).build();
        }
        return ApplicationContext.builder().mainClass(modMainClass).beanResolutionTrace(BeanResolutionTraceMode.STANDARD_OUT).packages().build();
    }

    public static void init(ApplicationContext context, IEventBus modEventBus, ModContainer modContainer) {
        context.registerSingleton(IEventBus.class, modEventBus, Qualifiers.byName("modEventBus"));
        context.registerSingleton(IEventBus.class, NeoForge.EVENT_BUS, Qualifiers.byName("forgeEventBus"));
        context.registerSingleton(ModContainer.class, modContainer, Qualifiers.byName("modContainer"));
        context.start();
        Configurable.configure(context, modEventBus, modContainer);
        // ModLoader.modList.getModFileById("kasuga_lib").getFile().findResource("META-INF/micronaut/io.micronaut.inject.BeanDefinitionReference").toUri().toURL()
    }

}
