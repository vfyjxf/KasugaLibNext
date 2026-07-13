package lib.kasuga.registration.data_driven.handler;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;

public record BlockEntityDef(String beType, String parentBlockId, @Nullable JsonObject params) {}
