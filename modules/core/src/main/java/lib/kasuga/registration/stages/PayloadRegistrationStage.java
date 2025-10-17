package lib.kasuga.registration.stages;

import net.minecraft.Util;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Function;

public class PayloadRegistrationStage {
    private final Function<String, PayloadRegistrar> registrarFunction;

    public PayloadRegistrationStage(Function<String, PayloadRegistrar> registrarFunction) {
        this.registrarFunction = Util.memoize(registrarFunction);
    }

    public PayloadRegistrar getRegistrar(String version) {
        return registrarFunction.apply(version);
    }
}
