package lib.kasuga.test.registration;

import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibRegistry;
import lib.kasuga.registration.Registry;
import lib.kasuga.registration.minecraft.block.BlockReg;
import lib.kasuga.registration.minecraft.item.ItemReg;
import lib.kasuga.registration.minecraft.effect.EffectReg;
import lib.kasuga.test.registration.minecraft.block.TestAdvancedBlock;
import lib.kasuga.test.registration.minecraft.effect.TestAdvancedEffect;
import lib.kasuga.test.registration.minecraft.item.TestAdvancedItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Advanced configuration tests to verify that the modifier system
 * properly applies configurations to registered objects.
 */
@ExtendWith(EphemeralTestServerProvider.class)
public class ConfigurationTest {
    
    public static Registry registry = KasugaLibRegistry.getRegistryOf(KasugaLib.MODID);
    
    // Test complex block configurations
    public static BlockReg<TestAdvancedBlock> ADVANCED_BLOCK = BlockReg.of("advanced_test_block", TestAdvancedBlock::new)
            .destroyTime(50.0f)
            .explosionResistance(1200.0f)
            .noOcclusion()
            .pushReaction(PushReaction.IGNORE)
            .friction(0.8f)
            .speedFactor(1.2f)
            .jumpFactor(1.5f)
            .requiresCorrectToolForDrops()
            .setParent(registry);
    
    // Test complex item configurations
    public static ItemReg<TestAdvancedItem> ADVANCED_ITEM = ItemReg.of("advanced_test_item", TestAdvancedItem::new)
            .stacksTo(1)
            .durability(1561)
            .rarity(Rarity.EPIC)
            .fireResistant()
            .setNoRepair()
            .setParent(registry);
    
    // Test complex effect configurations with custom attributes
    public static EffectReg<TestAdvancedEffect> ADVANCED_EFFECT = EffectReg.of("advanced_test_effect",
            (category, color) -> new TestAdvancedEffect(category, color))
            .harmful()
            .color(128, 0, 128) // Purple color
            .attribute(effect -> {
                // Add custom effect logic here if needed
            })
            .setParent(registry);

    @Test
    public void testAdvancedBlockConfiguration(MinecraftServer server) {
        var block = ADVANCED_BLOCK.getEntry();
        assert block != null;
        
        // Test block properties
        var defaultState = block.defaultBlockState();
        // Test other accessible properties
        assert block.getExplosionResistance() == 1200.0f;
        assert block.getFriction() == 0.8f;
        assert block.getSpeedFactor() == 1.2f;
        assert block.getJumpFactor() == 1.5f;
        
        System.out.println("✓ Advanced block configuration tests passed");
    }

    @Test 
    public void testAdvancedItemConfiguration(MinecraftServer server) {
        var item = ADVANCED_ITEM.getEntry();
        assert item != null;
        
        // Test item properties
        assert item.getMaxStackSize(item.getDefaultInstance()) == 1;
        assert item.getMaxDamage(item.getDefaultInstance()) == 1561;
        // Test that item is bound to registry
        assert item.builtInRegistryHolder().isBound();
        
        System.out.println("✓ Advanced item configuration tests passed");
    }

    @Test
    public void testAdvancedEffectConfiguration(MinecraftServer server) {
        var effect = ADVANCED_EFFECT.getEntry();
        assert effect != null;
        
        // Test effect properties
        assert effect.getCategory() == MobEffectCategory.HARMFUL;
        assert effect.getColor() == 0x800080; // Purple color (128, 0, 128)
        
        System.out.println("✓ Advanced effect configuration tests passed");
    }

    @Test
    public void testConfigurationChaining(MinecraftServer server) {
        // Test that configuration methods can be chained properly
        // This is verified by the successful creation of the above registrations
        // with multiple chained configuration calls
        
        assert ADVANCED_BLOCK.getEntry() != null;
        assert ADVANCED_ITEM.getEntry() != null;
        assert ADVANCED_EFFECT.getEntry() != null;
        
        System.out.println("✓ Configuration chaining tests passed");
    }
}
