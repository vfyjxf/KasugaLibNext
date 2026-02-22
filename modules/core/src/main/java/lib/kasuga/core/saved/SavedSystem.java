package lib.kasuga.core.saved;

import io.micronaut.context.annotation.Context;
import lib.kasuga.KasugaLibApplication;
import lib.kasuga.registration.minecraft.data_component.DataComponentReg;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

@Context()
public class SavedSystem {
    public static DataComponentReg<UUID> ITEM_SAVE_ID = new DataComponentReg<UUID>("item_external_id",
            (builder)->builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC)
    ).setParent(KasugaLibApplication.REGISTRY);
}
