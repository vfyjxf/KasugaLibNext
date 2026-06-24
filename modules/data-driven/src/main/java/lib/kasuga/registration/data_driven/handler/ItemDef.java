package lib.kasuga.registration.data_driven.handler;

import com.google.gson.JsonObject;

public record ItemDef(String id, String type, String registryGroup, JsonObject properties) {}
