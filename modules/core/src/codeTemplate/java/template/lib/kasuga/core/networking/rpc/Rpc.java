package template.lib.kasuga.core.networking.rpc;

import lib.kasuga.core.networking.rpc.RpcApi;
import lib.kasuga.internal.generator.annotations.CodeTemplate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

@CodeTemplate(generator = "Rpc")
public class Rpc {
    public static class TemplateV extends RpcApi.WithOutResponse<TemplateV.Request, TemplateV> {
        private final RpcFunction function;
        @FunctionalInterface
        public interface RpcFunction {
            public void call(IPayloadContext context);
        }

        public class Request {
            public Request() {}
        }

        private StreamCodec<FriendlyByteBuf, Request> codec;


        public TemplateV(
                String name,
                RpcFunction f
        ) {
            super(name);
            this.function = f;
            this.codec = new StreamCodec<FriendlyByteBuf, Request>() {
                @Override
                public Request decode(FriendlyByteBuf byteBuf) {
                    return new Request();
                }
                @Override
                public void encode(FriendlyByteBuf o, Request request) {}
            };
        }
        @Override
        protected StreamCodec<FriendlyByteBuf, Request> getRequestPayloadCodec() {
            return this.codec;
        }

        public void call(Player player) {
            this.call(new Request(), player);
        }

        @Override
        public void handleInstant(Request request, IPayloadContext context) {
            this.function.call(context);
        }
    }

    public static class TemplateR<R> extends RpcApi.WithResponse<TemplateR<R>.Request, R, TemplateR<R>> {
        private final RpcFunction<R> function;
        private final StreamCodec<? super FriendlyByteBuf, R> responseCodecs;

        @FunctionalInterface
        public interface RpcFunction<R> {
            public R call(IPayloadContext context);
        }

        public class Request {
            public Request() {}
        }

        private StreamCodec<? super FriendlyByteBuf, Request> codec;


        public TemplateR(
                String name,
                RpcFunction<R> f,
                StreamCodec<? super FriendlyByteBuf, R> responseCodecs
        ) {
            super(name);
            this.function = f;
            this.responseCodecs = responseCodecs;
            this.codec = new StreamCodec<FriendlyByteBuf, Request>() {
                @Override
                public Request decode(FriendlyByteBuf byteBuf) {
                    return new Request();
                }
                @Override
                public void encode(FriendlyByteBuf o, Request request) {}
            };
        }
        @Override
        protected StreamCodec<? super FriendlyByteBuf, Request> getRequestPayloadCodec() {
            return this.codec;
        }

        @Override
        protected StreamCodec<? super FriendlyByteBuf, R> getResponsePayloadCodec() {
            return this.responseCodecs;
        }

        public CompletableFuture<R> call(Player player) {
            return this.run(new Request(), player);
        }

        @Override
        public R handleInstant(Request request, IPayloadContext context) {
            return this.function.call(context);
        }
    }
}
