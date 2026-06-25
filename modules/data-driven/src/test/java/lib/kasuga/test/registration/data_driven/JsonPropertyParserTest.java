package lib.kasuga.test.registration.data_driven;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lib.kasuga.registration.data_driven.property.JsonPropertyParser;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class JsonPropertyParserTest {

    private final JsonPropertyParser parser = JsonPropertyParser.getInstance();

    private Consumer<BlockBehaviour.Properties> parseSingle(String key, Object value) {
        JsonObject json = new JsonObject();
        if (value instanceof Boolean b) json.addProperty(key, b);
        else if (value instanceof Number n) json.addProperty(key, n);
        else if (value instanceof String s) json.addProperty(key, s);
        List<Consumer<BlockBehaviour.Properties>> mods = parser.parseBlockProperties(json);
        assertEquals(1, mods.size(), "Expected exactly 1 modifier for key '" + key + "'");
        return mods.get(0);
    }

    private void assertModifierApplies(String key, Object value) {
        Consumer<BlockBehaviour.Properties> mod = parseSingle(key, value);
        assertNotNull(mod, "Modifier for '" + key + "' should not be null");
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of();
        mod.accept(props);
        assertNotNull(props, "Consumer should not crash");
    }

    // --- Boolean properties ---

    @Test
    void parseNoOcclusion() { assertModifierApplies("no_occlusion", true); }

    @Test
    void parseNoCollission() { assertModifierApplies("no_collission", true); }

    @Test
    void parseNoCollisionAltSpelling() { assertModifierApplies("no_collision", true); }

    @Test
    void parseReplaceable() { assertModifierApplies("replaceable", true); }

    @Test
    void parseDynamicShape() { assertModifierApplies("dynamic_shape", true); }

    @Test
    void parseRandomTicks() { assertModifierApplies("random_ticks", true); }

    @Test
    void parseRequiresCorrectTool() { assertModifierApplies("requires_correct_tool", true); }

    @Test
    void parseNoLootTable() { assertModifierApplies("no_loot_table", true); }

    @Test
    void parseIgnitedByLava() { assertModifierApplies("ignited_by_lava", true); }

    @Test
    void parseLiquid() { assertModifierApplies("liquid", true); }

    @Test
    void parseForceSolidOn() { assertModifierApplies("force_solid_on", true); }

    @Test
    void parseAir() { assertModifierApplies("air", true); }

    @Test
    void parseNoTerrainParticles() { assertModifierApplies("no_terrain_particles", true); }

    // --- Strength ---

    @Test
    void parseStrengthSingle() { assertModifierApplies("strength", 2.5f); }

    @Test
    void parseStrengthPair() {
        JsonObject json = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add(1.5);
        arr.add(3.0);
        json.add("strength", arr);
        List<Consumer<BlockBehaviour.Properties>> mods = parser.parseBlockProperties(json);
        assertEquals(1, mods.size());
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of();
        mods.get(0).accept(props);
        assertNotNull(props);
    }

    // --- Float properties ---

    @Test
    void parseDestroyTime() { assertModifierApplies("destroy_time", 4.0f); }

    @Test
    void parseExplosionResistance() { assertModifierApplies("explosion_resistance", 10.0f); }

    @Test
    void parseFriction() { assertModifierApplies("friction", 0.8f); }

    @Test
    void parseSpeedFactor() { assertModifierApplies("speed_factor", 0.5f); }

    @Test
    void parseJumpFactor() { assertModifierApplies("jump_factor", 1.5f); }

    // --- Map color ---

    @Test
    void parseMapColor() { assertModifierApplies("map_color", "blue"); }

    // --- Sound type ---

    @Test
    void parseSoundType() { assertModifierApplies("sound_type", "metal"); }

    // --- Light emission ---

    @Test
    void parseLightEmission() { assertModifierApplies("light_emission", 15); }

    // --- Edge cases ---

    @Test
    void nullInputReturnsEmptyList() {
        List<Consumer<BlockBehaviour.Properties>> mods = parser.parseBlockProperties(null);
        assertNotNull(mods);
        assertTrue(mods.isEmpty());
    }

    @Test
    void unknownPropertyReturnsEmpty() {
        JsonObject json = new JsonObject();
        json.addProperty("nonexistent_property", true);
        List<Consumer<BlockBehaviour.Properties>> mods = parser.parseBlockProperties(json);
        assertTrue(mods.isEmpty());
    }

    @Test
    void multiplePropertiesParsed() {
        JsonObject json = new JsonObject();
        json.addProperty("no_occlusion", true);
        json.addProperty("destroy_time", 2.0f);
        json.addProperty("friction", 0.9f);
        List<Consumer<BlockBehaviour.Properties>> mods = parser.parseBlockProperties(json);
        assertEquals(3, mods.size());
    }

    @Test
    void emptyJsonObjectReturnsEmptyList() {
        List<Consumer<BlockBehaviour.Properties>> mods = parser.parseBlockProperties(new JsonObject());
        assertTrue(mods.isEmpty());
    }
}
