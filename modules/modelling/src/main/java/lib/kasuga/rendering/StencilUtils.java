package lib.kasuga.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lib.kasuga.rendering.buffer.StencilMultiBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/* Copied from Crate's Stencil Element, following the Create Catnip's MIT LICENSE */
public class StencilUtils {
    public static void prepareStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(~0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.stencilFunc(GL11.GL_NEVER, 128, 0xFF);
    }

    public static void renderStencil() {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilFunc(GL11.GL_LEQUAL, 128, 0xFF);
    }

    public static void resetStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    public static void stencilRect(MultiBufferSource buffer, PoseStack pose, float minX, float minY, float maxX, float maxY, float z) {
        var builder = buffer.getBuffer(StencilMultiBuffer.STENCIL);
        var matrix = pose.last().pose();

        builder.addVertex(matrix, (float)minX, (float)minY, (float)z).setColor(0);
        builder.addVertex(matrix, (float)minX, (float)maxY, (float)z).setColor(0);
        builder.addVertex(matrix, (float)maxX, (float)maxY, (float)z).setColor(0);
        builder.addVertex(matrix, (float)maxX, (float)minY, (float)z).setColor(0);
    }
}
