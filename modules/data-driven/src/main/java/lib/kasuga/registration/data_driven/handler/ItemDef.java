package lib.kasuga.registration.data_driven.handler;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;

public record ItemDef(String id, String type, String registryGroup, JsonObject properties, @Nullable JsonObject params) {}
