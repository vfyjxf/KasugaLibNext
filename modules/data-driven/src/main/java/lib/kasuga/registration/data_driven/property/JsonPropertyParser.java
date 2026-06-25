package lib.kasuga.registration.data_driven.property;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import lib.kasuga.registration.data_driven.property.compiler.ModifierCompiler;
import lib.kasuga.registration.data_driven.property.compiler.RLCompiler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class JsonPropertyParser {

    public static final JsonPropertyParser INSTANCE = new JsonPropertyParser();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, SoundType> soundTypes;
    private final List<ModifierCompiler> compilers;

    public List<Consumer<BlockBehaviour.Properties>> parseBlockProperties(JsonObject json) {
        if (json == null) return List.of();
        List<Consumer<BlockBehaviour.Properties>> result = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            Consumer<BlockBehaviour.Properties> mod = parseBlockProperty(entry.getKey(), entry.getValue());
            if (mod != null) {
                result.add(mod);
            }
        }
        return result;
    }

    private Consumer<BlockBehaviour.Properties> parseBlockProperty(String key, JsonElement value) {
        try {
            Consumer<BlockBehaviour.Properties> mod = null;
            for (ModifierCompiler compiler : compilers) {
                mod = compiler.parse(key, value);
                if (mod != null) break;
            }
            if (mod == null) {
                LOGGER.warn("Unknown block property: {}", key);
            }
            return mod;
        } catch (Exception e) {
            LOGGER.warn("Failed to parse block property '{}': {}", key, e.getMessage());
            return null;
        }
    }

    private static boolean bool(JsonElement value) {
        return value.getAsBoolean();
    }

    private Consumer<BlockBehaviour.Properties> parseStrength(JsonElement value) {
        if (value.isJsonArray()) {
            var arr = value.getAsJsonArray();
            if (arr.size() == 2) {
                float destroyTime = arr.get(0).getAsFloat();
                float explosionResistance = arr.get(1).getAsFloat();
                return props -> props.strength(destroyTime, explosionResistance);
            }
        }
        float strength = value.getAsFloat();
        return props -> props.strength(strength);
    }

    private Consumer<BlockBehaviour.Properties> parseMapColor(JsonElement value) {
        String name = value.getAsString();
        DyeColor dye = DyeColor.byName(name, null);
        if (dye == null) {
            LOGGER.warn("Unknown map color: {}", name);
            return null;
        }
        return props -> props.mapColor(dye);
    }

    private Consumer<BlockBehaviour.Properties> parseSoundType(JsonElement value) {
        String name = value.getAsString();
        SoundType type = soundTypes.get(name);
        if (type == null) {
            LOGGER.warn("Unknown sound type: {}", name);
            return null;
        }
        return props -> props.sound(type);
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
        addCompiler(compilers, "no_occlusion", constructFunction(JsonPropertyParser::bool, props -> props.noOcclusion(), null));
        addCompiler(compilers, "no_collision", constructFunction(JsonPropertyParser::bool, props -> props.noCollission(), null));
        addCompiler(compilers, "no_collission", constructFunction(JsonPropertyParser::bool, props -> props.noCollission(), null));
        addCompiler(compilers, "requires_correct_tool", constructFunction(JsonPropertyParser::bool, props -> props.requiresCorrectToolForDrops(), null));
        addCompiler(compilers, "replaceable", constructFunction(JsonPropertyParser::bool, props -> props.replaceable(), null));
        addCompiler(compilers, "dynamic_shape", constructFunction(JsonPropertyParser::bool, props -> props.dynamicShape(), null));
        addCompiler(compilers, "random_ticks", constructFunction(JsonPropertyParser::bool, props -> props.randomTicks(), null));
        addCompiler(compilers, "no_loot_table", constructFunction(JsonPropertyParser::bool, props -> props.noLootTable(), null));
        addCompiler(compilers, "ignited_by_lava", constructFunction(JsonPropertyParser::bool, props -> props.ignitedByLava(), null));
        addCompiler(compilers, "liquid", constructFunction(JsonPropertyParser::bool, props -> props.liquid(), null));
        addCompiler(compilers, "force_solid_on", constructFunction(JsonPropertyParser::bool, props -> props.forceSolidOn(), null));
        addCompiler(compilers, "air", constructFunction(JsonPropertyParser::bool, props -> props.air(), null));
        addCompiler(compilers, "no_terrain_particles", constructFunction(JsonPropertyParser::bool, props -> props.noTerrainParticles(), null));
        addCompiler(compilers, "friction", (k, v) -> { float f = v.getAsFloat(); return props -> props.friction(f); });
        addCompiler(compilers, "speed_factor", (k, v) -> { float f = v.getAsFloat(); return props -> props.speedFactor(f); });
        addCompiler(compilers, "jump_factor", (k, v) -> { float f = v.getAsFloat(); return props -> props.jumpFactor(f); });
        addCompiler(compilers, "destroy_time", (k, v) -> { float f = v.getAsFloat(); return props -> props.destroyTime(f); });
        addCompiler(compilers, "explosion_resistance", (k, v) -> { float f = v.getAsFloat(); return props -> props.explosionResistance(f); });
        addCompiler(compilers, "strength", (k, v) -> parseStrength(v));
        addCompiler(compilers, "map_color", (k, v) -> parseMapColor(v));
        addCompiler(compilers, "sound_type", (k, v) -> parseSoundType(v));
        addCompiler(compilers, "light_emission", (k, v) -> { int level = v.getAsInt(); return props -> props.lightLevel(state -> level); });
        // TODO: 这里应该加入一个事件，使得其他用户可以在这里加入自己的编译器类型
        return compilers;
    }

    public static @NotNull BiFunction<String, JsonElement, Consumer<BlockBehaviour.Properties>> constructFunction(
            @NotNull Predicate<JsonElement> predicate,
            @NotNull Consumer<BlockBehaviour.Properties> prop,
            @Nullable Consumer<BlockBehaviour.Properties> defaultProp) {
        return (k, v) -> predicate.test(v) ? prop : defaultProp;
    }

    private static void addSound(Map<String, SoundType> map, String name, SoundType type) {
        map.put(name, type);
    }

    private static void addCompiler(List<ModifierCompiler> list, String name,
                                    BiFunction<String, JsonElement, Consumer<BlockBehaviour.Properties>> supplier) {
        list.add(new RLCompiler(ResourceLocation.tryParse(name.toLowerCase(Locale.ROOT)), supplier));
    }

    private JsonPropertyParser() {
        compilers = buildCompilers();
        soundTypes = buildSoundTypeMap();
    }

    public void registerCompiler(ModifierCompiler compiler) {
        compilers.add(compiler);
    }

    public void registerCompiler(String key, BiFunction<String, JsonElement, Consumer<BlockBehaviour.Properties>> supplier) {
        addCompiler(compilers, key, supplier);
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
