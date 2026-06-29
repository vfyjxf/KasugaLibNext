package lib.kasuga.registration.data_driven.context;

import lib.kasuga.registration.RegistryGroup;

public class JsonRegistryGroup extends RegistryGroup {
    private final String groupId;

    public JsonRegistryGroup(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }
}
