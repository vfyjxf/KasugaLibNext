package lib.kasuga.registration.data_driven.property;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class JsonItemParser {

    public static final JsonItemParser INSTANCE = new JsonItemParser();
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<String, ItemPropertyParser> parsers;

    private JsonItemParser() {
        parsers = new LinkedHashMap<>();

        parsers.put("stacks_to", (k, v) -> props -> props.stacksTo(v.getAsInt()));
        parsers.put("rarity", (k, v) -> {
            try {
                Rarity rarity = Rarity.valueOf(v.getAsString().toUpperCase(Locale.ROOT));
                return props -> props.rarity(rarity);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Unknown rarity: {}", v.getAsString());
                return null;
            }
        });
        parsers.put("fire_resistant", (k, v) -> v.getAsBoolean() ? props -> props.fireResistant() : null);
        parsers.put("durability", (k, v) -> props -> props.durability(v.getAsInt()));
        parsers.put("no_repair", (k, v) -> v.getAsBoolean() ? props -> props.setNoRepair() : null);
    }

    public List<Consumer<Item.Properties>> parseItemProperties(JsonObject json) {
        if (json == null) return List.of();
        List<Consumer<Item.Properties>> result = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            Consumer<Item.Properties> mod = parseItemProperty(entry.getKey(), entry.getValue());
            if (mod != null) {
                result.add(mod);
            }
        }
        return result;
    }

    private Consumer<Item.Properties> parseItemProperty(String key, JsonElement value) {
        // Skip "tab" — handled separately by JsonTreeBuilder for creative tab registration
        if ("tab".equals(key)) return null;

        ItemPropertyParser parser = parsers.get(key);
        if (parser != null) {
            try {
                return parser.parse(key, value);
            } catch (Exception e) {
                LOGGER.warn("Failed to parse item property '{}': {}", key, e.getMessage());
                return null;
            }
        }
        LOGGER.warn("Unknown item property: {}", key);
        return null;
    }

    public void registerParser(String key, ItemPropertyParser parser) {
        parsers.put(key, parser);
    }

    @FunctionalInterface
    public interface ItemPropertyParser {
        Consumer<Item.Properties> parse(String key, JsonElement value);
    }
}
