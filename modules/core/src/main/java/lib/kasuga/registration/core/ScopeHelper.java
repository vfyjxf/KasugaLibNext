package lib.kasuga.registration.core;

import lib.kasuga.registration.Reg;

import java.util.function.Supplier;

public class ScopeHelper {
    public static <T> Modifier<T> effect(ModifierType<T> effect, Supplier<T> value) {
        return new Modifier<T>() {
            @Override
            public ModifierType<T> getType() {
                return effect;
            }

            @Override
            public T transform(T originalValue) {
                return value.get();
            }
        };
    }

    public static <T> T get(Reg<?, ?> self, ModifierType<T> effect) {
        return self.transform(effect, null);
    }
}
