package lib.kasuga.content.device;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.function.BiFunction;

public record DeviceAttributeType<T extends DeviceAttribute>(
        BiFunction<CompoundTag, HolderLookup.Provider, T> factory,
        boolean isUnique,
        boolean canIndex,
        boolean persistent) {
    public static class Builder<T extends DeviceAttribute> {
        private final BiFunction<CompoundTag, HolderLookup.Provider, T> factory;

        private boolean isUnique = false;
        private boolean canIndex = false;
        private boolean persistent = true;

        public Builder(BiFunction<CompoundTag, HolderLookup.Provider, T> factory) {
            this.factory = factory;
        }

        public DeviceAttributeType<T> build() {
            return new DeviceAttributeType<>(factory, isUnique, canIndex, persistent);
        }

        public Builder<T> unique() {
            this.isUnique = true;
            return this;
        }

        public Builder<T> indexable() {
            this.canIndex = true;
            return this;
        }

        public Builder<?> notPersistent() {
            this.persistent = false;
            return this;
        }
    }
}
