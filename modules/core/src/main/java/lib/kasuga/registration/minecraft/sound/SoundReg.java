package lib.kasuga.registration.minecraft.sound;

import lib.kasuga.registration.core.ModifierType;
import lib.kasuga.registration.core.ScopeHelper;
import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Function;

/**
 * Use this registration to register your custom sound event to minecraft.
 * This replaces the old SoundReg class and follows the new registration system patterns.
 */
public final class SoundReg extends MinecraftDeferRegistryReg<SoundReg, SoundEvent, SoundEvent> {
    
    // 定义SCOPE用于自引用
    public static final ModifierType<SoundEvent> SCOPE = new ModifierType<>(true);
    
    private final Function<SoundReg, Function<ResourceLocation, SoundEvent>> supplier;

    /**
     * Create a sound reg with a fixed range sound event.
     * @param name the registration name.
     * @param range the hearing range of the sound.
     * @return a new SoundReg instance.
     */
    public static SoundReg of(String name, float range) {
        return new SoundReg(name, reg -> location -> SoundEvent.createFixedRangeEvent(location, range));
    }

    /**
     * Create a sound reg with a variable range sound event.
     * @param name the registration name.
     * @return a new SoundReg instance.
     */
    public static SoundReg of(String name) {
        return new SoundReg(name, reg -> SoundEvent::createVariableRangeEvent);
    }

    /**
     * Create a sound reg with a custom supplier.
     * @param name the registration name.
     * @param supplier the sound event supplier.
     * @return a new SoundReg instance.
     */
    public static SoundReg of(String name, Function<ResourceLocation, SoundEvent> supplier) {
        return new SoundReg(name, reg -> supplier);
    }

    public SoundReg(String name, Function<SoundReg, Function<ResourceLocation, SoundEvent>> supplier) {
        super(name, Registries.SOUND_EVENT);
        this.supplier = supplier;
        this.configure(ScopeHelper.effect(SCOPE, this::getEntry));
    }

    @Override
    protected SoundEvent createObject(ResourceLocation id) {
        return supplier.apply(this).apply(id);
    }
}
