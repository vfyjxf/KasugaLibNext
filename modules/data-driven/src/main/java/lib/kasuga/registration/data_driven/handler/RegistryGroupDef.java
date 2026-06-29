package lib.kasuga.registration.data_driven.handler;

import com.google.gson.JsonObject;

public record RegistryGroupDef(String id, String parent, JsonObject properties, JsonObject itemProperties) {}
