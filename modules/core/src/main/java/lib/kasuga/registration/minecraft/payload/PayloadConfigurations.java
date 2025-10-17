package lib.kasuga.registration.minecraft.payload;

import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.IAdaptedObject;
import lib.kasuga.registration.core.IModifierConfigure;
import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.minecraft.block.BlockConfigurations;

import java.util.function.Consumer;

public interface PayloadConfigurations<S> extends IModifierConfigure<S> {
    static abstract class ConsumeAdapter implements PayloadConfigurations<PayloadConfigurations.ConsumeAdapter>, IAdaptedObject<Reg<?, ?>> {}

    public static <T extends Reg<T, ?>> Consumer<T> adaptConsume(Consumer<PayloadConfigurations.ConsumeAdapter> s){
        return (i)->s.accept(new PayloadConfigurations.ConsumeAdapter(){
            @Override
            public Reg<?, ?> getOriginal() {
                return i;
            }

            @Override
            public ConsumeAdapter configure(Modifier<?> modifier) {
                i.configure(modifier);
                return this;
            }
        });
    }

    public default S version(String version) {
        return configure(PayloadModifiers.VERSION.apply(version));
    }

    public default S stage(PayloadStage stage) {
        return configure(PayloadModifiers.STAGE.apply(stage));
    }
}
