package lib.kasuga.rendering.models.mc.backend;

import com.mojang.blaze3d.vertex.PoseStack;
import lib.kasuga.rendering.models.mc.util.RayTracingHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class UIBackend {

    public static GuiGraphics constructGuiGraphics(PoseStack stack) {
        return new GuiGraphics(
                Minecraft.getInstance(),
                Minecraft.getInstance().renderBuffers().bufferSource()
        );
    }

    public static GuiGraphics constructGuiGraphics() {
        return constructGuiGraphics(new PoseStack());
    }

    public static void suppressZAxis(PoseStack stack) {
        stack.scale(1f, 1f, 1f / (GuiGraphics.MAX_GUI_Z - GuiGraphics.MIN_GUI_Z));
    }

    public static void renderRenderable(GuiGraphics guiGraphics, float partialTicks, Renderable renderable) {
        Vector2i size = getRenderableSize(renderable);
        guiGraphics.pose().pushPose();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 pos = camera.getPosition();
        guiGraphics.pose().translate(- pos.x(), - pos.y(), - pos.z());
        guiGraphics.pose().scale(1f, -1f, 1f);
        if (size == null) {
            renderable.render(guiGraphics, -1, -1, partialTicks);
        } else {
            int mouseX = -1, mouseY = -1;
            PoseStack pose = guiGraphics.pose();
            pose.scale(1f / (float) guiGraphics.guiHeight(), 1f / (float) guiGraphics.guiHeight(), 1f);
            pose.translate(-0.5f, -0.5f, 0);
            suppressZAxis(pose);
            Vector3f rayOrigin = new Vector3f(), rayDirection = new Vector3f(), dstHitPoint = new Vector3f();
            RayTracingHelper.getRayFromCamera(rayOrigin, rayDirection);
            float distance = RayTracingHelper.intersectRayWithPlane(
                    rayOrigin, rayDirection,
                    pose.last().pose(), pose.last().normal(),
                    dstHitPoint, true
            );
            if (distance < 5) {
                Matrix4f inv = new Matrix4f(pose.last().pose()).invert();
                inv.transformPosition(dstHitPoint);
                mouseX = Math.round(dstHitPoint.x - 0.5f * size.x);
                mouseY = Math.round(dstHitPoint.y - 0.5f * size.y);
            }
            renderable.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
        guiGraphics.pose().popPose();
    }



    public static Vector2i getRenderableSize(Renderable renderable) {
        if (renderable instanceof Screen screen) {
            return new Vector2i(screen.width, screen.height);
        } else if (renderable instanceof LayoutElement element) {
            return new Vector2i(element.getWidth(), element.getHeight());
        } else {
            return null;
        }
    }
}
