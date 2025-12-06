package lib.kasuga.registration.kasuga.device.attribute;

import lib.kasuga.registration.core.IChildrenConfiguration;
import lib.kasuga.registration.core.IModifierConfigure;

public interface DeviceAttributeConfigurations<S> extends IModifierConfigure<S> {
    default S indexable() {
        return configure(DeviceAttributeModifiers.INDEXABLE);
    }

    default S unique() {
        return configure(DeviceAttributeModifiers.UNIQUE);
    }

    default S notPersistent() {
        return configure(DeviceAttributeModifiers.NOT_PERSISTENT);
    }
}
