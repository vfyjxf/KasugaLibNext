package lib.kasuga.content.device;

import com.mojang.logging.LogUtils;
import io.micronaut.context.annotation.Context;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lib.kasuga.core.saved.CustomSavedDataManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Context()
public class GlobalDeviceManager {
    private Logger LOGGER = LogUtils.getLogger();
    HashMap<UUID, Device> devices = new HashMap<>();
    GlobalDeviceSavedData.Customized manager;

    @Inject() CustomSavedDataManager dataManager;

    AttributeIndex index = new AttributeIndex();

    public GlobalDeviceManager(@Named("forgeEventBus")IEventBus eventBus) {
        eventBus.addListener(this::onServerStarted);
        eventBus.addListener(this::onServerStopped);
    }
    public void onServerStarted(ServerStartedEvent event) {
        manager = dataManager.get(
                GlobalDeviceSavedData.Customized.class,
                (p,r,f)->new GlobalDeviceSavedData.Customized(this, p, r, f)
        );
        HashMap<UUID, GlobalDeviceSavedData> allSavedData = new HashMap<>();
        for (String savedName : manager.getAllSavedNames()) {
            try{
                GlobalDeviceSavedData data = manager.computeIfAbsent(savedName);
                UUID id = UUID.fromString(savedName);
                devices.put(id, data.create());
                allSavedData.put(id, data);
            }catch (RuntimeException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        index.reindex(allSavedData);
    }

    public void onServerStopped(ServerStoppingEvent event) {
        manager = null;
    }

    public void saveToData() {
        for (Map.Entry<UUID, Device> idDeviceEntry : this.devices.entrySet()) {
            if(!idDeviceEntry.getValue().isDirty())
                continue;
            GlobalDeviceSavedData savedData = manager.computeIfAbsent(idDeviceEntry.getKey().toString());
            savedData.load(idDeviceEntry.getValue());
            savedData.setDirty();

        }
    }

    public void addAttribute(UUID deviceId, DeviceAttribute attribute) {
        if(!this.devices.containsKey(deviceId))
            return;
        index.notifyAttributeAdded(deviceId, attribute);
        manager.computeIfAbsent(deviceId.toString()).attributes.add(attribute);
    }

    public void removeAttribute(UUID deviceId, DeviceAttribute attribute) {
        if(!this.devices.containsKey(deviceId))
            return;
        index.notifyAttributeRemoved(deviceId, attribute);
        manager.computeIfAbsent(deviceId.toString()).attributes.remove(attribute);
    }

    public List<DeviceAttribute> getAttributes(UUID deviceId) {
        if(!this.devices.containsKey(deviceId))
            return List.of();
        return manager.computeIfAbsent(deviceId.toString()).attributes;
    }

    public void remove(UUID deviceId) {
        if(!this.devices.containsKey(deviceId))
            return;
        for (DeviceAttribute attribute : getAttributes(deviceId)) {
            index.notifyAttributeRemoved(deviceId, attribute);
        }
        this.devices.remove(deviceId);
        manager.remove(deviceId.toString());
    }

    public void merge(UUID target, UUID from) {
        if(!this.devices.containsKey(target) || !this.devices.containsKey(from))
            return;
        Device targetDevice = this.devices.get(target);
        Device fromDevice = this.devices.get(from);
        remove(from);
        targetDevice.acceptMerge(fromDevice);
    }
}
