package test.kasuga.core.content.document;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lib.kasuga.content.document.DocumentComponentRenderer;
import lib.kasuga.content.qr.QrCodeCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class TestDocument {

    public static class CR implements DocumentComponentRenderer<String> {

        @Override
        public void render(String componentValue, ItemStack stack, MultiBufferSource bufferSource, PoseStack poseStack, int light) {
            // poseStack.mulPose(Axis.XN.rotationDegrees(90));
            //
//            Minecraft.getInstance().font.drawInBatch(
//                    componentValue,
//                    15,
//                    15,
//                    -1,
//                    false,
//                    poseStack.last().pose(),
//                    bufferSource,
//                    Font.DisplayMode.NORMAL,
//                    0,
//                    light
//            );
            poseStack.pushPose();Minecraft.getInstance().font.drawInBatch(
                    "北京西 ↔ 深圳北",
                   25,
                    45,
                    -1,
                    false,
                    poseStack.last().pose(),
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    light
            );
            poseStack.translate(80,64,0);
            poseStack.scale(.5f,.5f,.5f);
            VertexConsumer vc = bufferSource.getBuffer(RenderType.textBackground());
            try {
                BitMatrix matrix = QrCodeCache.encodeBitMatrix("https://www.bilibili.com/video/BV1GJ411x7h7");
                for(int x=0;x<matrix.getWidth();x++) {
                    for (int y = 0; y < matrix.getHeight(); y++) {
                        if (matrix.get(x, y)) {
                            vc.addVertex(poseStack.last().pose(), x, y, 0)
                                    .setColor(0, 0, 0, 255)
                                    .setLight(light);
                            vc.addVertex(poseStack.last().pose(), x, y + 1, 0)
                                    .setColor(0, 0, 0, 255)
                                    .setLight(light);
                            vc.addVertex(poseStack.last().pose(), x  + 1, y + 1, 0)
                                    .setColor(0, 0, 0, 255)
                                    .setLight(light);

                            vc.addVertex(poseStack.last().pose(), x  + 1, y, 0)
                                    .setColor(0, 0, 0, 255)
                                    .setLight(light);
                        }
                    }
                }
                poseStack.popPose();
            } catch (WriterException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
