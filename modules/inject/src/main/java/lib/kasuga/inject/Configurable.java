package lib.kasuga.inject;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public interface Configurable {
    public default void onConfigure(ModConfig.Type type, ModConfigSpec.Builder builder) {
        if(type == ModConfig.Type.CLIENT)
            configureClient(builder);
        else if(type == ModConfig.Type.SERVER)
            configureServer(builder);
        else if(type == ModConfig.Type.COMMON)
            configureCommon(builder);
        else if(type == ModConfig.Type.STARTUP)
            configureStartup(builder);
    }

    public default void configureClient(ModConfigSpec.Builder builder){}
    public default void configureServer(ModConfigSpec.Builder builder){}
    public default void configureCommon(ModConfigSpec.Builder builder){}
    public default void configureStartup(ModConfigSpec.Builder builder){}
}
