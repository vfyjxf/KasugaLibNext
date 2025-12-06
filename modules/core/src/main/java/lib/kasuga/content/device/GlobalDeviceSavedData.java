package lib.kasuga.content.device;

import com.mojang.datafixers.DataFixer;
import lib.kasuga.core.saved.ScopedNbtSavedData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GlobalDeviceSavedData extends SavedData {

    public static Factory<GlobalDeviceSavedData> FACTORY = new Factory<>(GlobalDeviceSavedData::new, GlobalDeviceSavedData::new);

    protected ResourceLocation type;

    protected CompoundTag data = new CompoundTag();

    protected List<DeviceAttribute> attributes = new ArrayList<>();

    public GlobalDeviceSavedData(CompoundTag tag, HolderLookup.Provider provider) {
        data = tag.getCompound("deviceData");

        CompoundTag attributes = tag.getCompound("attributes");

        for (String allKey : attributes.getAllKeys()) {
            ResourceLocation location = ResourceLocation.tryParse(allKey);
            if(location == null)
                continue;
            CompoundTag attributeTag = attributes.getCompound(allKey);
            DeviceAttributeType<?> type =
                    DeviceRegistries.DEVICE_ATTRIBUTE.get(
                            ResourceKey.create(DeviceRegistries.DEVICE_ATTRIBUTE_KEY, location)
                    );
            if (type != null) {
                DeviceAttribute attribute = type.factory().apply(attributeTag.getCompound("data"), provider);
                this.attributes.add(attribute);
            }
        }
    }

    public GlobalDeviceSavedData() {}

    public void load(Device device) {
        data = device.save(new CompoundTag());
        this.type = DeviceRegistries.DEVICE.getKey(device.getType());
    }

    public void save(Device device) {
        if(DeviceRegistries.DEVICE.getKey(device.getType()) != this.type) {
            return;
        }
        device.load(this.data);
    }

    public Device create() {
        if(this.type == null)
            return null;
        DeviceType<?> deviceType = DeviceRegistries.DEVICE.get(this.type);
        if(deviceType == null) {
            return null;
        }
        Device device = deviceType.create();
        device.load(this.data);
        return device;
    }

    public static class Customized extends ScopedNbtSavedData<GlobalDeviceSavedData> {

        private final GlobalDeviceManager deviceManager;

        public Customized(GlobalDeviceManager deviceManager, Path path, HolderLookup.Provider provider, DataFixer fixer) {
            super("devices", FACTORY, path, provider, fixer);
            this.deviceManager = deviceManager;
        }

        @Override
        public void save(Path path, HolderLookup.Provider provider) {
            deviceManager.saveToData();
            super.save(path, provider);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.put("deviceData", data);

        CompoundTag attributesTag = new CompoundTag();

        for (DeviceAttribute attribute : attributes) {
            CompoundTag attributeTag = new CompoundTag();
            attributeTag.put("data", attribute.save(new CompoundTag(), provider));
            String key = DeviceRegistries.DEVICE_ATTRIBUTE.getKey(attribute.getType()).toString();
            attributesTag.put(key, attributeTag);
        }
        compoundTag.put("attributes", attributesTag);
        return compoundTag;
    }
}
