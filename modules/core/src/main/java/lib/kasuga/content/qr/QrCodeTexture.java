package lib.kasuga.content.qr;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;

public class QrCodeTexture extends DynamicTexture {
    public QrCodeTexture(NativeImage pixels) {
        super(pixels);
    }
}
