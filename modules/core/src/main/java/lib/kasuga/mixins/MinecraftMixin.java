package lib.kasuga.mixins;

import lib.kasuga.core.resource.ClientResourceReloadFinishEvent;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "onResourceLoadFinished", at = @At("RETURN"))
    private void injectOnResourceLoadFinished(CallbackInfo ci){
        NeoForge.EVENT_BUS.post(new ClientResourceReloadFinishEvent());
    }
}
