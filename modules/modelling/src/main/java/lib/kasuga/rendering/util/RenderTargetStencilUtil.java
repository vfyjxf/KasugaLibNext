package lib.kasuga.rendering.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;

public class RenderTargetStencilUtil {
    public static void enable() {
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        if(!main.isStencilEnabled()) {
            main.enableStencil();
        }
    }
}
