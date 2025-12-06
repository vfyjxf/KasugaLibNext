package lib.kasuga.registration.kasuga.device.attribute;

import lib.kasuga.content.device.DeviceAttributeType;
import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.core.ModifierType;

public class DeviceAttributeModifiers {
    public static final ModifierType<DeviceAttributeType.Builder<?>> DEVICE_ATTRIBUTE = new ModifierType<>();

    public static class ProcessModifier extends Modifier<DeviceAttributeType.Builder<?>> {
        @Override
        public ModifierType<DeviceAttributeType.Builder<?>> getType() {
            return DEVICE_ATTRIBUTE;
        }
    }

    public static final ProcessModifier INDEXABLE = new ProcessModifier() {
        @Override
        public DeviceAttributeType.Builder<?> transform(DeviceAttributeType.Builder<?> originalValue) {
            return originalValue.indexable();
        }
    };

    public static final ProcessModifier UNIQUE = new ProcessModifier() {
        @Override
        public DeviceAttributeType.Builder<?> transform(DeviceAttributeType.Builder<?> originalValue) {
            return originalValue.unique();
        }
    };

    public static final ProcessModifier NOT_PERSISTENT = new ProcessModifier() {
        @Override
        public DeviceAttributeType.Builder<?> transform(DeviceAttributeType.Builder<?> originalValue) {
            return originalValue.notPersistent();
        }
    };
}
