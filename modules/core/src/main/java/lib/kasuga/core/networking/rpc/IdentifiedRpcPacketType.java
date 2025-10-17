package lib.kasuga.core.networking.rpc;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class IdentifiedRpcPacketType<T> {

    public Lazy<CustomPacketPayload.Type<Packet>> TYPE;
    public Lazy<StreamCodec<FriendlyByteBuf, IdentifiedRpcPacketType<T>.Packet>> codec;

    public IdentifiedRpcPacketType(Supplier<CustomPacketPayload.Type<IdentifiedRpcPacketType<T>.Packet>> type, Supplier<StreamCodec<? super FriendlyByteBuf, T>> payloadCodec) {
        this.codec = Lazy.of(()->StreamCodec.composite(
                ByteBufCodecs.VAR_LONG,
                Packet::getId,
                payloadCodec.get(),
                Packet::getValue,
                Packet::new
        ));
        this.TYPE = Lazy.of(type);
    }

    public StreamCodec<FriendlyByteBuf, Packet> getCodec() {
        return codec.get();
    }

    public Packet wrap(Long id, T value) {
        return new Packet(id, value);
    }

    public class Packet implements CustomPacketPayload {
        Long id;
        T value;
        public Packet(Long id, T value) {
            this.id = id;
            this.value = value;
        }

        public Long getId() {
            return id;
        }

        public T getValue() {
            return value;
        }

        @Override
        public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE.get();
        }
    }
}
