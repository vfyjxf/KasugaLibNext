package lib.kasuga.scripting;

import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lib.kasuga.core.resource.ClientResourceReloadFinishEvent;
import lib.kasuga.inject.class_loader.BeanOnlyIn;
import lib.kasuga.scripting.client.ScriptEngineErrorScreen;
import lib.kasuga.scripting.client.ScriptEngineMissingScreen;
import lib.kasuga.scripting.discovery.PackageSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;

import java.util.Set;

@Context()
@BeanOnlyIn.Client
public class ScriptClientApplication {

    @Inject() @Named("forgeEventBus")
    IEventBus eventBus;

    @Inject()
    PackageSystem packageSystem;

    @PostConstruct()
    public void init() {
        eventBus.addListener(this::onResourceReloadFinished);
    }

    private void onResourceReloadFinished(ClientResourceReloadFinishEvent event) {

        Set<String> missingEngines = packageSystem.getMissingEngines();

        if(!missingEngines.isEmpty()) {
            Minecraft.getInstance().setScreen(new ScriptEngineMissingScreen(
                    missingEngines,
                    Minecraft.getInstance().screen
            ));
        }

        if(!packageSystem.getEngineErrors().isEmpty()) {
            Minecraft.getInstance().setScreen(new ScriptEngineErrorScreen(
                    packageSystem.getEngineErrors(),
                    Minecraft.getInstance().screen
            ));
        }
    }


}
