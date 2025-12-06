package lib.kasuga.content.device;

import java.util.function.Supplier;

public class DeviceType<T extends Device> {
    private Supplier<T> factory;
    public DeviceType(Supplier<T> factory) {
        this.factory = factory;
    }
    public T create() {
        return this.factory.get();
    }
}
