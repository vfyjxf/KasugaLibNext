package lib.kasuga.rendering.models.mc.compat.iris;

import net.irisshaders.iris.pbr.loader.PBRTextureLoaderRegistry;

public class IrisConstants {

    public static void register() {
        PBRTextureLoaderRegistry.INSTANCE.register(KasugaTextureAtlas.class, new KasugaPBRLoader());
    }
}
