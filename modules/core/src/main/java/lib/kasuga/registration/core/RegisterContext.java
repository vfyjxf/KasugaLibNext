package lib.kasuga.registration.core;

import lib.kasuga.structure.Pair;
import lib.kasuga.registration.stages.RegistrationStage;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RegisterContext<L> {
    private RegistrationStage<L> stage;

    private L value;

    public RegisterContext(RegistrationStage<L> stage, L context) {
        this.value = context;
        this.stage = stage;
    }

    public <T> void onStage(RegistrationStage<T> stage, Consumer<T> consumer) {
        if(Objects.equals(stage, this.stage)) {
            //noinspection unchecked
            consumer.accept((T) value);
        }
    }
}
