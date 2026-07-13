package lib.kasuga.slp.javet;

import lib.kasuga.scripting.security.Api;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ScriptingTestApi {

    @Api
    public void printText(String text) {
        Minecraft.getInstance().tell(() -> {
            var player = Minecraft.getInstance().player;
            if(player != null) {
                player.displayClientMessage(Component.literal(text), true);
            }
        });
    }
}
