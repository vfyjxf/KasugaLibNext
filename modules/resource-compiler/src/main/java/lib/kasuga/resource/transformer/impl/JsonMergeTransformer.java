package lib.kasuga.resource.transformer.impl;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lib.kasuga.resource.model.LoadedResource;
import lib.kasuga.resource.model.SourceResource;
import lib.kasuga.resource.transformer.Transformer;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonMergeTransformer implements Transformer {

    private static final int PRIORITY = 100;

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public boolean shouldTransform(Set<SourceResource> resources) {
        return resources.size() > 1
                && resources.stream().allMatch(r -> r.file().getName().toLowerCase(Locale.ROOT).endsWith(".json"));
    }

    @Override
    public Set<LoadedResource> transform(Set<LoadedResource> loadedResources) {
        List<LoadedResource> sorted = loadedResources.stream()
                .sorted(Comparator.comparing(r -> r.source() != null ? r.source().file().getName() : ""))
                .collect(Collectors.toList());

        JsonObject merged = new JsonObject();
        for (LoadedResource res : sorted) {
            String sourceName = res.source() != null ? res.source().file().getName() : "<unknown>";
            String json = new String(res.data(), StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            deepMerge(merged, obj, sourceName);
        }

        byte[] result = new GsonBuilder().setPrettyPrinting().create()
                .toJson(merged)
                .getBytes(StandardCharsets.UTF_8);
        return Set.of(new LoadedResource(null, result));
    }

    private void deepMerge(JsonObject target, JsonObject source, String sourceName) {
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (target.has(key)) {
                JsonElement existing = target.get(key);
                if (existing.isJsonObject() && value.isJsonObject()) {
                    deepMerge(existing.getAsJsonObject(), value.getAsJsonObject(), sourceName);
                } else if (existing.isJsonArray() && value.isJsonArray()) {
                    JsonArray merged = new JsonArray();
                    merged.addAll(existing.getAsJsonArray());
                    merged.addAll(value.getAsJsonArray());
                    target.add(key, merged);
                } else {
                    System.out.printf("[WARN] JSON primitive conflict: key \"%s\" from \"%s\", overwriting previous value%n",
                            key, sourceName);
                    target.add(key, value);
                }
            } else {
                target.add(key, value);
            }
        }
    }
}
