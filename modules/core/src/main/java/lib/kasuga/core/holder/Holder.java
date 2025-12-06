package lib.kasuga.core.holder;

import java.util.function.Consumer;

public interface Holder<T> {

    public void attach(Provider<T> provider);
    public void detach();

    public static <T> Holder<T> simple(Consumer<T> applier, Consumer<T> disposer) {
        return new Simple<>(applier, disposer);
    }

    public static class Simple<T> implements Holder<T> {

        private final Consumer<T> applier;

        private final Consumer<T> disposer;
        private Provider<T> provider;
        private T instance;

        public Simple(Consumer<T> applier, Consumer<T> disposer) {
            this.applier = applier;
            this.disposer = disposer;
        }

        public void attach(Provider<T> provider) {
            if(this.provider != null) {
                if(this.provider == provider) {
                    return;
                }
                detach();
            }
            this.provider = provider;
            this.instance = provider.hold(this);
            this.applier.accept(instance);
        }

        public void detach() {
            if(this.provider != null) {
                this.disposer.accept(this.instance);
                this.provider.release(this);
                this.provider = null;
                this.instance = null;
            }
        }
    }
}
