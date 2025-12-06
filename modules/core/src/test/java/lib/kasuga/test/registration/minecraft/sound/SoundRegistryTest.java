package lib.kasuga.test.registration.minecraft.sound;

import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibRegistry;
import lib.kasuga.registration.Registry;
import lib.kasuga.registration.minecraft_old.sound.SoundReg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class SoundRegistryTest {
    
    public static Registry registry = KasugaLibRegistry.getRegistryOf(KasugaLib.MODID);
    
    public static SoundReg TEST_FIXED_RANGE_SOUND = SoundReg.of("test_fixed_range_sound", 16.0f)
            .setParent(registry);
    
    public static SoundReg TEST_VARIABLE_RANGE_SOUND = SoundReg.of("test_variable_range_sound")
            .setParent(registry);
    
    public static SoundReg TEST_CUSTOM_SOUND = SoundReg.of("test_custom_sound", 
            location -> SoundEvent.createFixedRangeEvent(location, 32.0f))
            .setParent(registry);

    @Test
    public void testSoundRegistry(MinecraftServer server) {
        // Test that sounds are registered
        assert TEST_FIXED_RANGE_SOUND.getEntry() != null;
        assert TEST_VARIABLE_RANGE_SOUND.getEntry() != null;
        assert TEST_CUSTOM_SOUND.getEntry() != null;
        
        // Test registry contains the sounds
        var soundRegistry = server.registryAccess().registryOrThrow(Registries.SOUND_EVENT);
        assert soundRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_fixed_range_sound"));
        assert soundRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_variable_range_sound"));
        assert soundRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_custom_sound"));
        
        // Test sound properties
        SoundEvent fixedRangeSound = soundRegistry.get(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_fixed_range_sound"));
        SoundEvent variableRangeSound = soundRegistry.get(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_variable_range_sound"));
        SoundEvent customSound = soundRegistry.get(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_custom_sound"));
        
        assert fixedRangeSound != null;
        assert variableRangeSound != null;
        assert customSound != null;
        
        // Test sound locations
        assert fixedRangeSound.getLocation().equals(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_fixed_range_sound"));
        assert variableRangeSound.getLocation().equals(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_variable_range_sound"));
        assert customSound.getLocation().equals(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_custom_sound"));
    }
}
