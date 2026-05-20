package lib.kasuga.registration.data_driven;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.minecraft.block.BlockRegModifiers;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;

import java.util.*;

public class JsonPropertyParser {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, SoundType> SOUND_TYPES = buildSoundTypeMap();

    public static List<Modifier<BlockBehaviour.Properties>> parseBlockProperties(JsonObject json) {
        if (json == null) return List.of();
        List<Modifier<BlockBehaviour.Properties>> result = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            Modifier<BlockBehaviour.Properties> mod = parseBlockProperty(entry.getKey(), entry.getValue());
            if (mod != null) {
                result.add(mod);
            }
        }
        return result;
    }

    private static Modifier<BlockBehaviour.Properties> parseBlockProperty(String key, JsonElement value) {
        try {
            return switch (key) {
                case "no_occlusion" -> bool(value) ? BlockRegModifiers.NO_OCCLUSION : null;
                case "no_collission" -> bool(value) ? BlockRegModifiers.NO_COLLISSION : null;
                case "requires_correct_tool" -> bool(value) ? BlockRegModifiers.REQUIRES_CORRECT_TOOL_FOR_DROPS : null;
                case "replaceable" -> bool(value) ? BlockRegModifiers.REPLACEABLE : null;
                case "dynamic_shape" -> bool(value) ? BlockRegModifiers.DYNAMIC_SHAPE : null;
                case "random_ticks" -> bool(value) ? BlockRegModifiers.RANDOM_TICKS : null;
                case "no_loot_table" -> bool(value) ? BlockRegModifiers.NO_LOOT_TABLE : null;
                case "ignited_by_lava" -> bool(value) ? BlockRegModifiers.IGNITED_BY_LAVA : null;
                case "liquid" -> bool(value) ? BlockRegModifiers.LIQUID : null;
                case "force_solid_on" -> bool(value) ? BlockRegModifiers.FORCE_SOLID_ON : null;
                case "air" -> bool(value) ? BlockRegModifiers.AIR : null;
                case "no_terrain_particles" -> bool(value) ? BlockRegModifiers.NO_TERRAIN_PARTICLES : null;
                case "friction" -> BlockRegModifiers.FRICTION_BY_FLOAT.apply(value.getAsFloat());
                case "speed_factor" -> BlockRegModifiers.SPEED_FACTOR_BY_FLOAT.apply(value.getAsFloat());
                case "jump_factor" -> BlockRegModifiers.JUMP_FACTOR_BY_FLOAT.apply(value.getAsFloat());
                case "destroy_time" -> BlockRegModifiers.DESTROY_TIME_BY_FLOAT.apply(value.getAsFloat());
                case "explosion_resistance" -> BlockRegModifiers.EXPLOSION_RESISTANCE_BY_FLOAT.apply(value.getAsFloat());
                case "strength" -> parseStrength(value);
                case "map_color" -> parseMapColor(value);
                case "sound_type" -> parseSoundType(value);
                case "light_emission" -> BlockRegModifiers.LIGHT_LEVEL_BY_TO_INT_FUNCTION.apply(state -> value.getAsInt());
                default -> {
                    LOGGER.warn("Unknown block property: {}", key);
                    yield null;
                }
            };
        } catch (Exception e) {
            LOGGER.warn("Failed to parse block property '{}': {}", key, e.getMessage());
            return null;
        }
    }

    private static boolean bool(JsonElement value) {
        return value.getAsBoolean();
    }

    private static Modifier<BlockBehaviour.Properties> parseStrength(JsonElement value) {
        if (value.isJsonArray()) {
            var arr = value.getAsJsonArray();
            if (arr.size() == 2) {
                return BlockRegModifiers.STRENGTH_BY_FLOAT_FLOAT.apply(
                    arr.get(0).getAsFloat(), arr.get(1).getAsFloat());
            }
        }
        return BlockRegModifiers.STRENGTH_BY_FLOAT.apply(value.getAsFloat());
    }

    private static Modifier<BlockBehaviour.Properties> parseMapColor(JsonElement value) {
        String name = value.getAsString();
        DyeColor dye = DyeColor.byName(name, null);
        if (dye != null) {
            return BlockRegModifiers.MAP_COLOR_BY_DYE_COLOR.apply(dye);
        }
        LOGGER.warn("Unknown map color: {}", name);
        return null;
    }

    private static Modifier<BlockBehaviour.Properties> parseSoundType(JsonElement value) {
        String name = value.getAsString();
        SoundType type = SOUND_TYPES.get(name);
        if (type != null) {
            return BlockRegModifiers.SOUND_BY_SOUND_TYPE.apply(type);
        }
        LOGGER.warn("Unknown sound type: {}", name);
        return null;
    }

    private static Map<String, SoundType> buildSoundTypeMap() {
        Map<String, SoundType> map = new HashMap<>();
        addSound(map, "stone", SoundType.STONE);
        addSound(map, "wood", SoundType.WOOD);
        addSound(map, "gravel", SoundType.GRAVEL);
        addSound(map, "grass", SoundType.GRASS);
        addSound(map, "sand", SoundType.SAND);
        addSound(map, "metal", SoundType.METAL);
        addSound(map, "glass", SoundType.GLASS);
        addSound(map, "wool", SoundType.WOOL);
        addSound(map, "wet_grass", SoundType.WET_GRASS);
        addSound(map, "coral_block", SoundType.CORAL_BLOCK);
        addSound(map, "lantern", SoundType.LANTERN);
        addSound(map, "stem", SoundType.STEM);
        addSound(map, "sweet_berry_bush", SoundType.SWEET_BERRY_BUSH);
        addSound(map, "crop", SoundType.CROP);
        addSound(map, "nether_wood", SoundType.NETHER_WOOD);
        addSound(map, "nether_ore", SoundType.NETHER_ORE);
        addSound(map, "nether_wart", SoundType.NETHER_WART);
        addSound(map, "basalt", SoundType.BASALT);
        addSound(map, "soul_sand", SoundType.SOUL_SAND);
        addSound(map, "bone_block", SoundType.BONE_BLOCK);
        addSound(map, "netherrack", SoundType.NETHERRACK);
        addSound(map, "nether_bricks", SoundType.NETHER_BRICKS);
        addSound(map, "shroomlight", SoundType.SHROOMLIGHT);
        addSound(map, "wart_block", SoundType.WART_BLOCK);
        addSound(map, "anvil", SoundType.ANVIL);
        addSound(map, "chain", SoundType.CHAIN);
        addSound(map, "copper", SoundType.COPPER);
        addSound(map, "calcite", SoundType.CALCITE);
        addSound(map, "tuff", SoundType.TUFF);
        addSound(map, "dripstone_block", SoundType.DRIPSTONE_BLOCK);
        addSound(map, "pointed_dripstone", SoundType.POINTED_DRIPSTONE);
        addSound(map, "moss_carpet", SoundType.MOSS_CARPET);
        addSound(map, "hanging_roots", SoundType.HANGING_ROOTS);
        addSound(map, "rooted_dirt", SoundType.ROOTED_DIRT);
        addSound(map, "big_dripleaf", SoundType.BIG_DRIPLEAF);
        addSound(map, "small_dripleaf", SoundType.SMALL_DRIPLEAF);
        addSound(map, "spore_blossom", SoundType.SPORE_BLOSSOM);
        addSound(map, "cave_vines", SoundType.CAVE_VINES);
        addSound(map, "powder_snow", SoundType.POWDER_SNOW);
        addSound(map, "amethyst_cluster", SoundType.AMETHYST_CLUSTER);
        addSound(map, "sponge", SoundType.SPONGE);
        addSound(map, "lily_pad", SoundType.LILY_PAD);
        addSound(map, "cobweb", SoundType.COBWEB);
        addSound(map, "suspicious_sand", SoundType.SUSPICIOUS_SAND);
        addSound(map, "suspicious_gravel", SoundType.SUSPICIOUS_GRAVEL);
        return map;
    }

    private static void addSound(Map<String, SoundType> map, String name, SoundType type) {
        map.put(name, type);
    }

    private JsonPropertyParser() {}
}
