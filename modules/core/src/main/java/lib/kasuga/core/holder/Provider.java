package lib.kasuga.core.holder;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.Optional;

public interface Provider<T>  {

    public T hold(Holder<T> holder);
    public void release(Holder<T> holder);
    public void dispose();

    public @Nullable T getValue();

    public default void ifPresent(Consumer<T> tConsumer) {
        T delegate = getValue();
        if(delegate != null)
            tConsumer.accept(delegate);
    }

    public static <T> Provider<T> simple(Supplier<T> creator, Consumer<T> disposer) {
        return new Simple<>(creator, disposer);
    }

    public class Simple<T> implements Provider<T> {
        private Set<Holder<T>> holders = new HashSet<>();

        private Supplier<T> creator;

        private Consumer<T> disposer;

        private T instance;

        public Simple(Supplier<T> creator, Consumer<T> disposer) {
            this.creator = creator;
            this.disposer = disposer;
        }

        public T hold(Holder<T> holder) {
            holders.add(holder);
            if (instance == null) {
                instance = creator.get();
            }
            return instance;
        }

        public void release(Holder<T> holder) {
            holders.remove(holder);
            if (holders.isEmpty() && instance != null) {
                disposer.accept(instance);
                instance = null;
            }
        }

        public void dispose() {
            for (Holder<T> holder : Set.copyOf(holders)) {
                holder.detach();
            }

            holders.clear();

            if (instance != null) {
                disposer.accept(instance);
                instance = null;
            }
        }

        @Nullable
        @Override
        public T getValue() {
            return instance;
        }
    }
}
