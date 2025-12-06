package lib.kasuga.content.document;

import com.mojang.serialization.Codec;
import lib.kasuga.KasugaLib;
import lib.kasuga.content.device.DeviceRegistries;
import lib.kasuga.core.codec.KasugaCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.HashMap;
import java.util.Map;

public class DocumentComponentRegistries {
    public static ResourceKey<Registry<DocumentComponentType<?>>> DOCUMENT_COMPONENT_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "document_component_type"));
    public static Registry<DocumentComponentType<?>> DOCUMENT_COMPONENT_REGISTRY = new RegistryBuilder<>(DOCUMENT_COMPONENT_KEY)
            .sync(true)
            .create();

    public static final Codec<Map<Holder<DocumentComponentType<?>>, Object>> DOCUMENT_COMPONENT_PERSISTENT = Codec.dispatchedMap(
            DOCUMENT_COMPONENT_REGISTRY.holderByNameCodec(),
            s->s.value().codec()
    );

    public static final StreamCodec<? super RegistryFriendlyByteBuf, Map<Holder<DocumentComponentType<?>>, Object>>
            DOCUMENT_COMPONENT_BYTE_BUF = KasugaCodec.mapFunc(
                    HashMap::new,
                    ByteBufCodecs.holderRegistry(DOCUMENT_COMPONENT_KEY),
                    (Holder<DocumentComponentType<?>> h)->h.value().networkCodec(),
                    Integer.MAX_VALUE,
                    true);

    public static void onRegistryEvent(NewRegistryEvent event) {
        event.register(DOCUMENT_COMPONENT_REGISTRY);
        event.register(DeviceRegistries.DEVICE);
        event.register(DeviceRegistries.DEVICE_ATTRIBUTE);
    }
}
