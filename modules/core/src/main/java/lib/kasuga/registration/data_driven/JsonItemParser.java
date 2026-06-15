package lib.kasuga.registration.data_driven;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.minecraft.item.ItemRegModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.slf4j.Logger;

import java.util.*;

public class JsonItemParser {

    public static final JsonItemParser INSTANCE = new JsonItemParser();
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<String, ItemPropertyParser> parsers;

    private JsonItemParser() {
        parsers = new LinkedHashMap<>();
        parsers.put("stacks_to", (k, v) -> ItemRegModifiers.STACKS_TO_BY_INT.apply(v.getAsInt()));
        parsers.put("rarity", (k, v) -> parseRarity(v.getAsString()));
        parsers.put("fire_resistant", (k, v) -> v.getAsBoolean() ? ItemRegModifiers.FIRE_RESISTANT : null);
        parsers.put("durability", (k, v) -> ItemRegModifiers.DURABILITY_BY_INT.apply(v.getAsInt()));
        parsers.put("no_repair", (k, v) -> v.getAsBoolean() ? ItemRegModifiers.SET_NO_REPAIR : null);
    }

    public List<Modifier<Item.Properties>> parseItemProperties(JsonObject json) {
        if (json == null) return List.of();
        List<Modifier<Item.Properties>> result = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            Modifier<Item.Properties> mod = parseItemProperty(entry.getKey(), entry.getValue());
            if (mod != null) {
                result.add(mod);
            }
        }
        return result;
    }

    private Modifier<Item.Properties> parseItemProperty(String key, JsonElement value) {
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

    private Modifier<Item.Properties> parseRarity(String name) {
        try {
            Rarity rarity = Rarity.valueOf(name.toUpperCase(Locale.ROOT));
            return ItemRegModifiers.RARITY_BY_RARITY.apply(rarity);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown rarity: {}", name);
            return null;
        }
    }

    public void registerParser(String key, ItemPropertyParser parser) {
        parsers.put(key, parser);
    }

    @FunctionalInterface
    public interface ItemPropertyParser {
        Modifier<Item.Properties> parse(String key, JsonElement value);
    }
}
