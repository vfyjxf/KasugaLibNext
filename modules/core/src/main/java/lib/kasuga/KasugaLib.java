package lib.kasuga;

import io.micronaut.context.ApplicationContext;

import lib.kasuga.test.TestMyRpc;
import lib.kasuga.inject.ModApplicationContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(KasugaLib.MODID)
public class KasugaLib {
    public static final ApplicationContext context = ModApplicationContext.create(KasugaLib.class);
    public static final String MODID = "kasuga_lib";

    public KasugaLib(IEventBus modEventBus, ModContainer modContainer) {
        ModApplicationContext.init(context, modEventBus, modContainer);
    }

    public static ApplicationContext getContext() {
        return context;
    }
}
