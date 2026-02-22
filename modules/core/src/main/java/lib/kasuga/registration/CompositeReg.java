package lib.kasuga.registration;

import java.util.Map;

public class CompositeReg<S extends CompositeReg<?>> extends Reg<S, Map<Class<? extends Registry>, Registry>> {
    public Map<Class<? extends Registry>, Registry> getEntry(){
        throw new IllegalStateException("This method should override in subclass, may failed to generate the code in codegen");
    }
}
