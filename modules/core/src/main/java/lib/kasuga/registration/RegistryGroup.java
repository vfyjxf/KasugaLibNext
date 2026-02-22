package lib.kasuga.registration;

import lib.kasuga.registration.core.IModifierConfigure;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class RegistryGroup extends Reg<RegistryGroup, Void> implements Consumer<Reg<?, ?>>, IModifierConfigure<RegistryGroup> {
    public List<Function<Reg<?, ?>, Reg<?, ?>>> transformation = new ArrayList<>();

    public RegistryGroup transform(Function<Reg<?, ?>, Reg<?, ?>> function) {
        this.transformation.add(function);
        return self();
    }

    @Override
    public void accept(Reg<?, ?> reg) {
        reg.setParent(this);
    }

    @Override
    public Void getEntry() {
        return null;
    }
}
