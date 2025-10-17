package lib.kasuga.registration.minecraft.payload;

import com.mojang.logging.LogUtils;
import lib.kasuga.inject.class_loader.Envs;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.RegisterContext;
import lib.kasuga.registration.core.ResourceLocationModifiers;
import lib.kasuga.registration.stages.RegistrationStage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import java.util.function.Supplier;

public class PayloadReg<T extends CustomPacketPayload> extends Reg<PayloadReg<T>, CustomPacketPayload.Type<T>> implements PayloadConfigurations<PayloadReg<T>> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final String name;

    private final Supplier<StreamCodec<FriendlyByteBuf, T>> codec;
    private CustomPacketPayload.Type<T> entry;
    private Supplier<IPayloadHandler<T>> serverHandlerSupplier;
    private Supplier<IPayloadHandler<T>> clientHandlerSupplier;

    public PayloadReg(String name, StreamCodec<FriendlyByteBuf, T> codec) {
        this(name, ()->codec);
    }

    public PayloadReg(String name, Supplier<StreamCodec<FriendlyByteBuf, T>> codec) {
        this.name = name;
        this.codec = codec;
    }

    @Override
    public void register(RegisterContext<?> context) {
        context.onStage(RegistrationStage.PAYLOAD_REGISTRATION, c->{
            this.ensureEntry();
            PayloadStage stage = transform(PayloadModifiers.STAGE_MODIFIER, PayloadStage.COMMON);
            String version = transform(PayloadModifiers.VERSION_MODIFIER, "1");
            PayloadRegistrar registrar = c.getRegistrar(version);
            IPayloadHandler<T> clientHandler = null;
            if(clientHandlerSupplier != null) {
                if(Envs.isDedicatedServer()){
                    clientHandler = (i,j)->{};
                } else {
                    clientHandler = clientHandlerSupplier.get();
                }
            }
            IPayloadHandler<T> serverHandler = null;
            if(serverHandlerSupplier != null) {
                serverHandler = serverHandlerSupplier.get();
            }

            StreamCodec<FriendlyByteBuf, T> codec = this.codec.get();

            if(serverHandler != null && clientHandler != null) {
                switch (stage) {
                    case PLAY -> registrar.playBidirectional(this.entry, codec, new DirectionalPayloadHandler<>(clientHandler, serverHandler));
                    case CONFIGURATION -> registrar.configurationBidirectional(this.entry, codec, new DirectionalPayloadHandler<>(clientHandler, serverHandler));
                    case COMMON -> registrar.commonBidirectional(this.entry, codec, new DirectionalPayloadHandler<>(clientHandler, serverHandler));
                    default -> throw new IllegalStateException("Unexpected value: " + stage);
                }
            } else if(serverHandler != null) {
                switch (stage) {
                    case PLAY -> registrar.playToServer(this.entry, codec, serverHandler);
                    case CONFIGURATION -> registrar.configurationToServer(this.entry, codec, serverHandler);
                    case COMMON -> registrar.commonToServer(this.entry, codec, serverHandler);
                    default -> throw new IllegalStateException("Unexpected value: " + stage);
                }
            } else if(clientHandler != null) {
                switch (stage) {
                    case PLAY -> registrar.playToClient(this.entry, codec, clientHandler);
                    case CONFIGURATION -> registrar.configurationToClient(this.entry, codec, clientHandler);
                    case COMMON -> registrar.commonToClient(this.entry, codec, clientHandler);
                    default -> throw new IllegalStateException("Unexpected value: " + stage);
                }
            } else {
                LOGGER.warn("No handlers for payload {}, skipping registration", this.name);
            }
        });
    }

    private synchronized void ensureEntry() {
        if(this.entry == null)
            this.entry = new CustomPacketPayload.Type<>(transform(ResourceLocationModifiers.ID, ResourceLocation.fromNamespaceAndPath("minecraft",name)));
    }

    public PayloadReg<T> server(Supplier<IPayloadHandler<T>> handler) {
        this.serverHandlerSupplier = handler;
        return this;
    }

    public PayloadReg<T> client(Supplier<IPayloadHandler<T>> handler) {
        this.clientHandlerSupplier = handler;
        return this;
    }

    @Override
    public CustomPacketPayload.Type<T> getEntry() {
        return entry;
    }
}
