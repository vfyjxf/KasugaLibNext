package lib.kasuga;

import cpw.mods.util.Lazy;
import io.micronaut.context.ApplicationContext;

import io.micronaut.inject.qualifiers.Qualifiers;
import lib.kasuga.inject.Configurable;
import lib.kasuga.inject.ModApplicationContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

@Mod(KasugaLib.MODID)
public class KasugaLib {
    public static final ApplicationContext context = ModApplicationContext.create(KasugaLib.class);
    public static final String MODID = "kasuga_lib";

    public KasugaLib(IEventBus modEventBus, ModContainer modContainer) {
        context.registerSingleton(IEventBus.class, modEventBus, Qualifiers.byName("modEventBus"));
        context.registerSingleton(ModContainer.class, modContainer, Qualifiers.byName("modContainer"));
        context.start();
        ModConfigSpec.Builder specClient = new ModConfigSpec.Builder();
        ModConfigSpec.Builder specServer = new ModConfigSpec.Builder();
        ModConfigSpec.Builder specCommon = new ModConfigSpec.Builder();
        ModConfigSpec.Builder specStartUp = new ModConfigSpec.Builder();
        for (Configurable configurable : context.getBeansOfType(Configurable.class)) {
            configurable.onConfigure(ModConfig.Type.CLIENT, specClient);
            configurable.onConfigure(ModConfig.Type.SERVER, specServer);
            configurable.onConfigure(ModConfig.Type.COMMON, specCommon);
            configurable.onConfigure(ModConfig.Type.STARTUP, specStartUp);
        }
        registerConfig(modContainer, ModConfig.Type.CLIENT, specClient);
        registerConfig(modContainer, ModConfig.Type.SERVER, specServer);
        registerConfig(modContainer, ModConfig.Type.COMMON, specCommon);
        registerConfig(modContainer, ModConfig.Type.STARTUP, specStartUp);
    }


    private static void registerConfig(ModContainer modContainer, ModConfig.Type type, ModConfigSpec.Builder specBuilder) {
        IConfigSpec spec = specBuilder.build();
        if(spec.isEmpty())
            return;
        modContainer.registerConfig(type, spec);
    }

    public static ApplicationContext getContext() {
        return context;
    }
}
