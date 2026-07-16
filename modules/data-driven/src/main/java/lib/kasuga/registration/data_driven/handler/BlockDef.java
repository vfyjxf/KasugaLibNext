package lib.kasuga.registration.data_driven.handler;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;

public record BlockDef(String id, String type, String registryGroup, JsonObject properties, JsonObject itemProperties, @Nullable JsonObject params) {}
