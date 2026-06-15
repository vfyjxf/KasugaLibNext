package lib.kasuga.registration.data_driven;

import java.util.LinkedHashMap;
import java.util.Map;

public class BuildContext {

    private final String modId;
    private final JsonRegistryGroup rootGroup;
    private final Map<String, Map<String, Object>> metaStore = new LinkedHashMap<>();

    public BuildContext(String modId, JsonRegistryGroup rootGroup) {
        this.modId = modId;
        this.rootGroup = rootGroup;
    }

    public String getModId() { return modId; }

    public JsonRegistryGroup getRootGroup() { return rootGroup; }

    @SuppressWarnings("unchecked")
    public <T> void putMeta(String typeName, String id, T value) {
        metaStore.computeIfAbsent(typeName, k -> new LinkedHashMap<>()).put(id, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMeta(String typeName, String id) {
        Map<String, Object> map = metaStore.get(typeName);
        return map != null ? (T) map.get(id) : null;
    }
}
