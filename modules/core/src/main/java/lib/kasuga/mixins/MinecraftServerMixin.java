package lib.kasuga.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lib.kasuga.KasugaLib;
import lib.kasuga.core.resource.ResourceSystem;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @WrapOperation(
            method = "reloadResources",
            at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenAcceptAsync(Ljava/util/function/Consumer;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;")
    )
    private <T> CompletableFuture<Void> onReloadResources(CompletableFuture<T> instance, Consumer<? super T> action, Executor executor, Operation<CompletableFuture<Void>> original) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        return original.call(instance, new Consumer<T>() {
            @Override
            public void accept(T object) {
                action.accept(object);
                if(!(object instanceof MinecraftServer.ReloadableResources))
                    return;
                KasugaLib.getContext().getBean(ResourceSystem.class)
                        .onServerReloading(server);
            }
        }, executor);
    }
}
