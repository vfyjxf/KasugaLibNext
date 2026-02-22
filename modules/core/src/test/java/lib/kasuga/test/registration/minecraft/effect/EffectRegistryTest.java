package lib.kasuga.test.registration.minecraft.effect;

import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibRegistry;
import lib.kasuga.registration.Registry;
import lib.kasuga.registration.minecraft.effect.EffectReg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class EffectRegistryTest {
    
    public static Registry registry = KasugaLibRegistry.getRegistryOf(KasugaLib.MODID);
    
    public static EffectReg<TestMobEffect> TEST_BENEFICIAL_EFFECT = EffectReg.of("test_beneficial_effect",
                    TestMobEffect::new)
            .category(MobEffectCategory.BENEFICIAL)
            .color(0x00ff00)
            .setParent(registry);
    
    public static EffectReg<TestMobEffect> TEST_HARMFUL_EFFECT = EffectReg.of("test_harmful_effect",
                    TestMobEffect::new)
            .category(MobEffectCategory.HARMFUL)
            .color(0xff0000)
            .setParent(registry);

    public static EffectReg<TestMobEffect> TEST_NEUTRAL_EFFECT = EffectReg.of("test_neutral_effect",
                    TestMobEffect::new)
            .category(MobEffectCategory.NEUTRAL)
            .color(0x0000ff)
            .setParent(registry);

    @Test
    public void testEffectRegistry(MinecraftServer server) {
        // Test that effects are registered
        assert TEST_BENEFICIAL_EFFECT.getEntry() != null;
        assert TEST_HARMFUL_EFFECT.getEntry() != null;
        
        // Test registry contains the effects
        var effectRegistry = server.registryAccess().registryOrThrow(Registries.MOB_EFFECT);
        assert effectRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_beneficial_effect"));
        assert effectRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_harmful_effect"));
        assert effectRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_neutral_effect"));
        
        // Test effect categories
        assert TEST_BENEFICIAL_EFFECT.getEntry().getCategory() == MobEffectCategory.BENEFICIAL;
        assert TEST_HARMFUL_EFFECT.getEntry().getCategory() == MobEffectCategory.HARMFUL;
        assert TEST_NEUTRAL_EFFECT.getEntry().getCategory() == MobEffectCategory.NEUTRAL;

        
        // Test effect colors
        assert TEST_BENEFICIAL_EFFECT.getEntry().getColor() == 0x00ff00;
        assert TEST_HARMFUL_EFFECT.getEntry().getColor() == 0xff0000;
        assert TEST_NEUTRAL_EFFECT.getEntry().getColor() == 0x0000ff;
    }
}
