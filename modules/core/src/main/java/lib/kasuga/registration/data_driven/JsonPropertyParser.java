package lib.kasuga.registration.data_driven;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.data_driven.compiler.ModifierCompiler;
import lib.kasuga.registration.data_driven.compiler.RLCompiler;
import lib.kasuga.registration.minecraft.block.BlockRegModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class JsonPropertyParser {

    public static final JsonPropertyParser INSTANCE = new JsonPropertyParser();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, SoundType> soundTypes;
    private final List<ModifierCompiler> compilers;

    public List<Modifier<BlockBehaviour.Properties>> parseBlockProperties(JsonObject json) {
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

    private Modifier<BlockBehaviour.Properties> parseBlockProperty(String key, JsonElement value) {
        try {
            Modifier<BlockBehaviour.Properties> mod = null;
            for (ModifierCompiler compiler : compilers) {
                mod = compiler.parse(key, value);
                if (mod != null) break;
            }
            if (mod == null) {
                LOGGER.warn("Unknown block property: {}", key);
            }
            return mod;
            // TODO: 若检查无问题后删除这地下注释掉的东西
//            return switch (key) {
//                case "no_occlusion" -> bool(value) ? BlockRegModifiers.NO_OCCLUSION : null;
//                case "no_collission" -> bool(value) ? BlockRegModifiers.NO_COLLISSION : null;
//                case "requires_correct_tool" -> bool(value) ? BlockRegModifiers.REQUIRES_CORRECT_TOOL_FOR_DROPS : null;
//                case "replaceable" -> bool(value) ? BlockRegModifiers.REPLACEABLE : null;
//                case "dynamic_shape" -> bool(value) ? BlockRegModifiers.DYNAMIC_SHAPE : null;
//                case "random_ticks" -> bool(value) ? BlockRegModifiers.RANDOM_TICKS : null;
//                case "no_loot_table" -> bool(value) ? BlockRegModifiers.NO_LOOT_TABLE : null;
//                case "ignited_by_lava" -> bool(value) ? BlockRegModifiers.IGNITED_BY_LAVA : null;
//                case "liquid" -> bool(value) ? BlockRegModifiers.LIQUID : null;
//                case "force_solid_on" -> bool(value) ? BlockRegModifiers.FORCE_SOLID_ON : null;
//                case "air" -> bool(value) ? BlockRegModifiers.AIR : null;
//                case "no_terrain_particles" -> bool(value) ? BlockRegModifiers.NO_TERRAIN_PARTICLES : null;
//                case "friction" -> BlockRegModifiers.FRICTION_BY_FLOAT.apply(value.getAsFloat());
//                case "speed_factor" -> BlockRegModifiers.SPEED_FACTOR_BY_FLOAT.apply(value.getAsFloat());
//                case "jump_factor" -> BlockRegModifiers.JUMP_FACTOR_BY_FLOAT.apply(value.getAsFloat());
//                case "destroy_time" -> BlockRegModifiers.DESTROY_TIME_BY_FLOAT.apply(value.getAsFloat());
//                case "explosion_resistance" -> BlockRegModifiers.EXPLOSION_RESISTANCE_BY_FLOAT.apply(value.getAsFloat());
//                case "strength" -> parseStrength(value);
//                case "map_color" -> parseMapColor(value);
//                case "sound_type" -> parseSoundType(value);
//                case "light_emission" -> BlockRegModifiers.LIGHT_LEVEL_BY_TO_INT_FUNCTION.apply(state -> value.getAsInt());
//                default -> {
//                    LOGGER.warn("Unknown block property: {}", key);
//                    yield null;
//                }
//            };
        } catch (Exception e) {
            LOGGER.warn("Failed to parse block property '{}': {}", key, e.getMessage());
            return null;
        }
    }

    private static boolean bool(JsonElement value) {
        return value.getAsBoolean();
    }

    private Modifier<BlockBehaviour.Properties> parseStrength(JsonElement value) {
        if (value.isJsonArray()) {
            var arr = value.getAsJsonArray();
            if (arr.size() == 2) {
                return BlockRegModifiers.STRENGTH_BY_FLOAT_FLOAT.apply(
                    arr.get(0).getAsFloat(), arr.get(1).getAsFloat());
            }
        }
        return BlockRegModifiers.STRENGTH_BY_FLOAT.apply(value.getAsFloat());
    }

    private Modifier<BlockBehaviour.Properties> parseMapColor(JsonElement value) {
        String name = value.getAsString();
        DyeColor dye = DyeColor.byName(name, null);
        if (dye != null) {
            return BlockRegModifiers.MAP_COLOR_BY_DYE_COLOR.apply(dye);
        }
        LOGGER.warn("Unknown map color: {}", name);
        return null;
    }

    private Modifier<BlockBehaviour.Properties> parseSoundType(JsonElement value) {
        String name = value.getAsString();
        SoundType type = soundTypes.get(name);
        if (type != null) {
            return BlockRegModifiers.SOUND_BY_SOUND_TYPE.apply(type);
        }
        LOGGER.warn("Unknown sound type: {}", name);
        return null;
    }

    private Map<String, SoundType> buildSoundTypeMap() {
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
        // TODO: 这里应该加入一个事件，使得其他用户可以在这里加入自己的声音类型
        return map;
    }

    public List<ModifierCompiler> buildCompilers() {
        List<ModifierCompiler> compilers = new ArrayList<>();
        addCompiler(compilers, "no_occlusion", constructFunction(JsonPropertyParser::bool, BlockRegModifiers.NO_OCCLUSION, null));
        // FIXME: 这里这个collission是不是写错了，应该写成collision.
        addCompiler(compilers, "no_collission", constructFunction(JsonPropertyParser::bool, BlockRegModifiers.NO_COLLISSION, null));
        addCompiler(compilers, "requires_correct_tool", constructFunction(JsonPropertyParser::bool, BlockRegModifiers.REQUIRES_CORRECT_TOOL_FOR_DROPS, null));
        addCompiler(compilers, "replaceable", constructFunction(JsonPropertyParser::bool, BlockRegModifiers.REPLACEABLE, null));
        addCompiler(compilers, "dynamic_shape", constructFunction(JsonPropertyParser::bool, BlockRegModifiers.DYNAMIC_SHAPE, null));
        addCompiler(compilers, "random_ticks", constructFunction(JsonPropertyParser::bool, BlockRegModifiers.RANDOM_TICKS, null));
        addCompiler(compilers, "no_loot_table", constructFunction(JsonPropertyParser::bool, BlockRegModifiers.NO_LOOT_TABLE, null));
        addCompiler(compilers, "ignited_by_lava", constructFunction(JsonPropertyParser::bool, BlockRegModifiers.IGNITED_BY_LAVA, null));
        addCompiler(compilers, "liquid", constructFunction(JsonPropertyParser::bool, BlockRegModifiers.LIQUID, null));
        addCompiler(compilers, "force_solid_on", constructFunction(JsonPropertyParser::bool, BlockRegModifiers.FORCE_SOLID_ON, null));
        addCompiler(compilers, "air", constructFunction(JsonPropertyParser::bool, BlockRegModifiers.AIR, null));
        addCompiler(compilers, "no_terrain_particles", constructFunction(JsonPropertyParser::bool, BlockRegModifiers.NO_TERRAIN_PARTICLES, null));
        addCompiler(compilers, "friction", (k, v) -> BlockRegModifiers.FRICTION_BY_FLOAT.apply(v.getAsFloat()));
        addCompiler(compilers, "speed_factor", (k, v) -> BlockRegModifiers.SPEED_FACTOR_BY_FLOAT.apply(v.getAsFloat()));
        addCompiler(compilers, "jump_factor", (k, v) -> BlockRegModifiers.JUMP_FACTOR_BY_FLOAT.apply(v.getAsFloat()));
        addCompiler(compilers, "destroy_time", (k, v) -> BlockRegModifiers.DESTROY_TIME_BY_FLOAT.apply(v.getAsFloat()));
        addCompiler(compilers, "explosion_resistance", (k, v) -> BlockRegModifiers.EXPLOSION_RESISTANCE_BY_FLOAT.apply(v.getAsFloat()));
        addCompiler(compilers, "strength", (k, v) -> parseStrength(v));
        addCompiler(compilers, "map_color", (k, v) -> parseMapColor(v));
        addCompiler(compilers, "sound_type", (k, v) -> parseSoundType(v));
        addCompiler(compilers, "light_emission", (k, v) -> BlockRegModifiers.LIGHT_LEVEL_BY_TO_INT_FUNCTION.apply(state -> v.getAsInt()));
        // TODO: 这里应该加入一个事件，使得其他用户可以在这里加入自己的编译器类型.
        return compilers;
    }

    public static @NotNull BiFunction<String, JsonElement, Modifier<BlockBehaviour.Properties>> constructFunction(@NotNull Predicate<JsonElement> predicate,
                                                                                                                  @NotNull Modifier<BlockBehaviour.Properties> prop,
                                                                                                                  @Nullable Modifier<BlockBehaviour.Properties> defaultProp) {
        return (k, v) -> predicate.test(v) ? prop : defaultProp;
    }


    private static void addSound(Map<String, SoundType> map, String name, SoundType type) {
        map.put(name, type);
    }

    private static void addCompiler(List<ModifierCompiler> list, String name,
                                    BiFunction<String, JsonElement, Modifier<BlockBehaviour.Properties>> supplier) {
        list.add(new RLCompiler(ResourceLocation.tryParse(name.toLowerCase(Locale.ROOT)), supplier));
    }

    private JsonPropertyParser() {
        compilers = buildCompilers();
        soundTypes = buildSoundTypeMap();
    }

    public void registerCompiler(ModifierCompiler compiler) {
        compilers.add(compiler);
    }

    protected void clearCompilers() {
        compilers.clear();
    }

    public void removeCompiler(ModifierCompiler compiler) {
        compilers.remove(compiler);
    }

    public int compilerSize() {
        return compilers.size();
    }

    public static JsonPropertyParser getInstance() {
        return INSTANCE;
    }
}
