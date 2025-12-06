package lib.kasuga.registration.minecraft_old.block;

import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.core.ModifierType;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class BlockModifiers {
    public static ModifierType<BlockBehaviour.Properties> TYPE_BLOCK_PROPERTIES = new ModifierType<>();

    public static abstract class SetBlockPropertiesModifier extends Modifier<BlockBehaviour.Properties> {
        @Override
        public ModifierType<BlockBehaviour.Properties> getType() {
            return TYPE_BLOCK_PROPERTIES;
        }

        public static SetBlockPropertiesModifier of(String name, Consumer<BlockBehaviour.Properties> setter) {
            return new SetBlockPropertiesModifier() {
                @Override
                public BlockBehaviour.Properties transform(BlockBehaviour.Properties originalValue) {
                    setter.accept(originalValue);
                    return originalValue;
                }
            };
        }
    }

    // 1. Map color
    public static Function<DyeColor, Modifier<BlockBehaviour.Properties>> MAP_COLOR_BY_DYE =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("mapColorByDye", (p) -> p.mapColor(i)));

    public static Function<MapColor, Modifier<BlockBehaviour.Properties>> MAP_COLOR_BY_MAP =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("mapColorByMap", (p) -> p.mapColor(i)));

    // 2. Collision / Occlusion
    public static Modifier<BlockBehaviour.Properties> NO_COLLISION =
            SetBlockPropertiesModifier.of("noCollision", BlockBehaviour.Properties::noCollission);

    public static Modifier<BlockBehaviour.Properties> NO_OCCLUSION =
            SetBlockPropertiesModifier.of("noOcclude", BlockBehaviour.Properties::noOcclusion);

    // 3. Friction / Speed / Jump
    public static Function<Float, Modifier<BlockBehaviour.Properties>> FRICTION =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("friction", (p) -> p.friction(i)));

    public static Function<Float, Modifier<BlockBehaviour.Properties>> SPEED_FACTOR =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("speedFactor", (p) -> p.speedFactor(i)));

    public static Function<Float, Modifier<BlockBehaviour.Properties>> JUMP_FACTOR =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("jumpFactor", (p) -> p.jumpFactor(i)));

    // 4. Sound
    public static Function<SoundType, Modifier<BlockBehaviour.Properties>> SOUND =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("sound", (p) -> p.sound(i)));

    // 5. Light
    public static Function<ToIntFunction<BlockState>, Modifier<BlockBehaviour.Properties>> LIGHT_LEVEL =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("lightLevel", (p) -> p.lightLevel(i)));

    // 6. Strength / Explosion Resistance
    public static BiFunction<Float, Float, Modifier<BlockBehaviour.Properties>> HARDNESS_AND_RESISTANCE =
            Util.memoize((h, r) -> SetBlockPropertiesModifier.of("hardnessAndResistance", (p) -> p.strength(h, r)));

    public static Function<Float, Modifier<BlockBehaviour.Properties>> DESTROY_TIME =
            Util.memoize((h) -> SetBlockPropertiesModifier.of("destroyTime", (p) -> p.destroyTime(h)));

    // 7. Instabreak
    public static Modifier<BlockBehaviour.Properties> INSTANT_BREAK =
            SetBlockPropertiesModifier.of("instant_break", BlockBehaviour.Properties::instabreak);

    // 8. Random ticks
    public static Modifier<BlockBehaviour.Properties> RANDOM_TICKS =
            SetBlockPropertiesModifier.of("randomTicks", BlockBehaviour.Properties::randomTicks);

    // 9. Dynamic shape
    public static Modifier<BlockBehaviour.Properties> DYNAMIC_SHAPE =
            SetBlockPropertiesModifier.of("dynamicShape", BlockBehaviour.Properties::dynamicShape);

    // 10. Loot / Drops
    public static Modifier<BlockBehaviour.Properties> NO_LOOT_TABLE =
            SetBlockPropertiesModifier.of("noLootTable", BlockBehaviour.Properties::noLootTable);

    // 11. Ignited by lava
    public static Modifier<BlockBehaviour.Properties> IGNITED_BY_LAVA =
            SetBlockPropertiesModifier.of("ignitedByLava", BlockBehaviour.Properties::ignitedByLava);

    // 12. Liquid
    public static Modifier<BlockBehaviour.Properties> LIQUID =
            SetBlockPropertiesModifier.of("liquid", BlockBehaviour.Properties::liquid);

    // 13. Force solid
    public static Modifier<BlockBehaviour.Properties> FORCE_SOLID_ON =
            SetBlockPropertiesModifier.of("forceSolidOn", BlockBehaviour.Properties::forceSolidOn);

    // 14. Push reaction
    public static Function<PushReaction, Modifier<BlockBehaviour.Properties>> PUSH_REACTION =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("pushReaction", (p) -> p.pushReaction(i)));

    // 15. Air
    public static Modifier<BlockBehaviour.Properties> AIR =
            SetBlockPropertiesModifier.of("air", BlockBehaviour.Properties::air);

    // 16. Spawn / Redstone / Suffocating / View blocking
    public static Function<BlockBehaviour.StateArgumentPredicate<EntityType<?>>, Modifier<BlockBehaviour.Properties>> VALID_SPAWN =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("isValidSpawn", (p) -> p.isValidSpawn(i)));

    public static Function<BlockBehaviour.StatePredicate, Modifier<BlockBehaviour.Properties>> REDSTONE_CONDUCTOR =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("isRedstoneConductor", (p) -> p.isRedstoneConductor(i)));

    public static Function<BlockBehaviour.StatePredicate, Modifier<BlockBehaviour.Properties>> SUFFOCATING =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("isSuffocating", (p) -> p.isSuffocating(i)));

    public static Function<BlockBehaviour.StatePredicate, Modifier<BlockBehaviour.Properties>> VIEW_BLOCKING =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("isViewBlocking", (p) -> p.isViewBlocking(i)));

    // 17. Post process / Emissive
    public static Function<BlockBehaviour.StatePredicate, Modifier<BlockBehaviour.Properties>> POST_PROCESS =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("hasPostProcess", (p) -> p.hasPostProcess(i)));

    public static Function<BlockBehaviour.StatePredicate, Modifier<BlockBehaviour.Properties>> EMISSIVE_RENDERING =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("emissiveRendering", (p) -> p.emissiveRendering(i)));

    // 18. Correct tool for drops
    public static Modifier<BlockBehaviour.Properties> REQUIRES_CORRECT_TOOL_FOR_DROPS =
            SetBlockPropertiesModifier.of("requiresCorrectToolForDrops", BlockBehaviour.Properties::requiresCorrectToolForDrops);

    // 19. Explosion resistance
    public static Function<Float, Modifier<BlockBehaviour.Properties>> EXPLOSION_RESISTANCE =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("explosionResistance", (p) -> p.explosionResistance(i)));

    // 20. Offset type
    public static Function<BlockBehaviour.OffsetType, Modifier<BlockBehaviour.Properties>> OFFSET_TYPE =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("offsetType", (p) -> p.offsetType(i)));

    // 21. Terrain particles
    public static Modifier<BlockBehaviour.Properties> NO_TERRAIN_PARTICLES =
            SetBlockPropertiesModifier.of("noTerrainParticles", BlockBehaviour.Properties::noTerrainParticles);

    // 22. Required features
    public static Function<FeatureFlag[], Modifier<BlockBehaviour.Properties>> REQUIRED_FEATURES =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("requiredFeatures", (p) -> p.requiredFeatures(i)));

    // 23. Instrument
    public static Function<NoteBlockInstrument, Modifier<BlockBehaviour.Properties>> INSTRUMENT =
            Util.memoize((i) -> SetBlockPropertiesModifier.of("instrument", (p) -> p.instrument(i)));

    // 24. Replaceable
    public static Modifier<BlockBehaviour.Properties> REPLACEABLE =
            SetBlockPropertiesModifier.of("replaceable", BlockBehaviour.Properties::replaceable);

}
