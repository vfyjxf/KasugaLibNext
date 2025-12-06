package lib.kasuga.registration.minecraft_old.block;

import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.IAdaptedObject;
import lib.kasuga.registration.core.IModifierConfigure;
import lib.kasuga.registration.core.Modifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Consumer;
import java.util.function.ToIntFunction;

public interface BlockConfigurations<S> extends IModifierConfigure<S>, BlockChildrenConfigurations<S> {

    static abstract class ConsumeAdapter implements BlockConfigurations<ConsumeAdapter>, IAdaptedObject<Reg<?, ?>> {}

    public static <T extends Reg<T, ?>> Consumer<T> adaptConsume(Consumer<ConsumeAdapter> s){
        return (i)->s.accept(new ConsumeAdapter(){
            @Override
            public Reg<?, ?> getOriginal() {
                return i;
            }

            @Override
            public ConsumeAdapter self() {
                return this;
            }

            @Override
            public ConsumeAdapter addChild(Reg<?, ?> child) {
                i.addChild(child);
                return this;
            }

            @Override
            public Iterable<Reg<?, ?>> getChildren() {
                return i.getChildren();
            }

            @Override
            public ConsumeAdapter configure(Modifier<?> modifier) {
                i.configure(modifier);
                return this;
            }
        });
    }

    // 1. Map color
    public default S withMapColor(DyeColor color) {
        return configure(BlockModifiers.MAP_COLOR_BY_DYE.apply(color));
    }

    public default S withMapColor(MapColor color) {
        return configure(BlockModifiers.MAP_COLOR_BY_MAP.apply(color));
    }

    // 2. Collision / Occlusion
    public default S noCollision() {
        return configure(BlockModifiers.NO_COLLISION);
    }

    public default S noOcclusion() {
        return configure(BlockModifiers.NO_OCCLUSION);
    }

    // 3. Friction / Speed / Jump
    public default S friction(float value) {
        return configure(BlockModifiers.FRICTION.apply(value));
    }

    public default S speedFactor(float value) {
        return configure(BlockModifiers.SPEED_FACTOR.apply(value));
    }

    public default S jumpFactor(float value) {
        return configure(BlockModifiers.JUMP_FACTOR.apply(value));
    }

    // 4. Sound
    public default S withSound(SoundType sound) {
        return configure(BlockModifiers.SOUND.apply(sound));
    }

    // 5. Light level
    public default S lightLevel(ToIntFunction<BlockState> light) {
        return configure(BlockModifiers.LIGHT_LEVEL.apply(light));
    }

    // 6. Strength / Explosion Resistance
    public default S hardnessAndResistance(float hardness, float resistance) {
        return configure(BlockModifiers.HARDNESS_AND_RESISTANCE.apply(hardness, resistance));
    }

    public default S destroyTime(float time) {
        return configure(BlockModifiers.DESTROY_TIME.apply(time));
    }

    public default S instantBreak() {
        return configure(BlockModifiers.INSTANT_BREAK);
    }

    // 7. Random ticks
    public default S randomTicks() {
        return configure(BlockModifiers.RANDOM_TICKS);
    }

    // 8. Dynamic shape
    public default S dynamicShape() {
        return configure(BlockModifiers.DYNAMIC_SHAPE);
    }

    // 9. Loot / Drops
    public default S noLootTable() {
        return configure(BlockModifiers.NO_LOOT_TABLE);
    }

    // 10. Ignited by lava
    public default S ignitedByLava() {
        return configure(BlockModifiers.IGNITED_BY_LAVA);
    }

    // 11. Liquid
    public default S liquid() {
        return configure(BlockModifiers.LIQUID);
    }

    // 12. Force solid
    public default S forceSolidOn() {
        return configure(BlockModifiers.FORCE_SOLID_ON);
    }

    // 13. Push reaction
    public default S pushReaction(PushReaction reaction) {
        return configure(BlockModifiers.PUSH_REACTION.apply(reaction));
    }

    // 14. Air
    public default S air() {
        return configure(BlockModifiers.AIR);
    }

    // 15. Spawn / Redstone / Suffocating / View blocking
    public default S isValidSpawn(BlockBehaviour.StateArgumentPredicate<EntityType<?>> predicate) {
        return configure(BlockModifiers.VALID_SPAWN.apply(predicate));
    }

    public default S isRedstoneConductor(BlockBehaviour.StatePredicate predicate) {
        return configure(BlockModifiers.REDSTONE_CONDUCTOR.apply(predicate));
    }

    public default S isSuffocating(BlockBehaviour.StatePredicate predicate) {
        return configure(BlockModifiers.SUFFOCATING.apply(predicate));
    }

    public default S isViewBlocking(BlockBehaviour.StatePredicate predicate) {
        return configure(BlockModifiers.VIEW_BLOCKING.apply(predicate));
    }

    // 16. Post process / Emissive
    public default S hasPostProcess(BlockBehaviour.StatePredicate predicate) {
        return configure(BlockModifiers.POST_PROCESS.apply(predicate));
    }

    public default S emissiveRendering(BlockBehaviour.StatePredicate predicate) {
        return configure(BlockModifiers.EMISSIVE_RENDERING.apply(predicate));
    }

    // 17. Correct tool for drops
    public default S requiresCorrectToolForDrops() {
        return configure(BlockModifiers.REQUIRES_CORRECT_TOOL_FOR_DROPS);
    }

    // 18. Explosion resistance
    public default S explosionResistance(float resistance) {
        return configure(BlockModifiers.EXPLOSION_RESISTANCE.apply(resistance));
    }

    // 19. Offset type
    public default S offsetType(BlockBehaviour.OffsetType type) {
        return configure(BlockModifiers.OFFSET_TYPE.apply(type));
    }

    // 20. Terrain particles
    public default S noTerrainParticles() {
        return configure(BlockModifiers.NO_TERRAIN_PARTICLES);
    }

    // 21. Required features
    public default S requiredFeatures(FeatureFlag... features) {
        return configure(BlockModifiers.REQUIRED_FEATURES.apply(features));
    }

    // 22. Instrument
    public default S instrument(NoteBlockInstrument instrument) {
        return configure(BlockModifiers.INSTRUMENT.apply(instrument));
    }

    // 23. Replaceable
    public default S replaceable() {
        return configure(BlockModifiers.REPLACEABLE);
    }


}
