package lib.kasuga.create.content.train.device;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class TrainDeviceRegistry {
    public static final HashMap<ResourceLocation, TrainDeviceSystemType<?>> REGISTRY = new HashMap<>();
    public static final HashMap<TrainDeviceSystemType<?>, ResourceLocation> REVERSE_REGISTRY = new HashMap<>();

    public static final Set<TrainDeviceSystemType<?>> CONTEXT = new HashSet<>();

    public static void register(ResourceLocation location, TrainDeviceSystemType<?> type) {
        register(location, type, false);
    }

    public static void register(ResourceLocation location, TrainDeviceSystemType<?> type, boolean contextDevice) {
        if (REGISTRY.containsKey(location)) {
            throw new IllegalArgumentException("Duplicate registration for " + location);
        }
        if (REVERSE_REGISTRY.containsKey(type)) {
            throw new IllegalArgumentException("Duplicate registration for " + type);
        }
        REGISTRY.put(location, type);
        REVERSE_REGISTRY.put(type, location);
        if(contextDevice) {
            CONTEXT.add(type);
        }
    }

    public static Map<TrainDeviceSystemType<?>, TrainDeviceSystem> createInitialContextDevices(TrainDeviceManager manager) {
        Map<TrainDeviceSystemType<?>,TrainDeviceSystem> devices = new HashMap<>();
        for(TrainDeviceSystemType<?> type : CONTEXT) {
            devices.put(type, type.create(manager));
        }
        return devices;
    }

    public static TrainDeviceSystemType<?> get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    public static ResourceLocation getKey(TrainDeviceSystemType<?> type) {
        return REVERSE_REGISTRY.get(type);
    }
}
