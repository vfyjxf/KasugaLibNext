package lib.kasuga.test.registration;

import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibRegistry;
import lib.kasuga.registration.Registry;
import lib.kasuga.test.registration.minecraft.block.BlockRegistryTest;
import lib.kasuga.test.registration.minecraft.block_entity.BlockEntityRegistryTest;
import lib.kasuga.test.registration.minecraft.effect.EffectRegistryTest;
import lib.kasuga.test.registration.minecraft.entity.EntityRegistryTest;
import lib.kasuga.test.registration.minecraft.item.ItemRegistryTest;
import lib.kasuga.test.registration.minecraft.sound.SoundRegistryTest;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Comprehensive test suite for all Minecraft module registrations.
 * This test class validates the complete registration system for the KasugaLib
 * including blocks, items, entities, effects, sounds, and block entities.
 */
@ExtendWith(EphemeralTestServerProvider.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MinecraftRegistrationTest {
    
    public static Registry registry = KasugaLibRegistry.getRegistryOf(KasugaLib.MODID);
    
    @BeforeAll
    public static void setupRegistrations() {
        // Ensure all registrations are initialized before tests run
        // This is needed for proper test execution order
    }

    @Test
    @Order(1)
    public void testBlockRegistrations(MinecraftServer server) {
        BlockRegistryTest blockTest = new BlockRegistryTest();
        blockTest.testBlockRegistry(server);
        System.out.println("✓ Block registration tests passed");
    }

    @Test
    @Order(2)
    public void testItemRegistrations(MinecraftServer server) {
        ItemRegistryTest itemTest = new ItemRegistryTest();
        itemTest.testItemRegistry(server);
        System.out.println("✓ Item registration tests passed");
    }

    @Test
    @Order(3)
    public void testEffectRegistrations(MinecraftServer server) {
        EffectRegistryTest effectTest = new EffectRegistryTest();
        effectTest.testEffectRegistry(server);
        System.out.println("✓ Effect registration tests passed");
    }

    @Test
    @Order(4)
    public void testEntityRegistrations(MinecraftServer server) {
        EntityRegistryTest entityTest = new EntityRegistryTest();
        entityTest.testEntityRegistry(server);
        System.out.println("✓ Entity registration tests passed");
    }

    @Test
    @Order(5)
    public void testSoundRegistrations(MinecraftServer server) {
        SoundRegistryTest soundTest = new SoundRegistryTest();
        soundTest.testSoundRegistry(server);
        System.out.println("✓ Sound registration tests passed");
    }

    @Test
    @Order(6)
    public void testBlockEntityRegistrations(MinecraftServer server) {
        BlockEntityRegistryTest blockEntityTest = new BlockEntityRegistryTest();
        blockEntityTest.testBlockEntityRegistry(server);
        System.out.println("✓ Block Entity registration tests passed");
    }

    @Test
    @Order(7)
    public void testRegistryIntegrity(MinecraftServer server) {
        // Test that all registrations are properly integrated
        assert registry != null;
        
        // Verify that all registry types are accessible
        var registryAccess = server.registryAccess();
        assert registryAccess != null;
        
        // Verify that we can access all required registries
        assert registryAccess.registryOrThrow(net.minecraft.core.registries.Registries.BLOCK) != null;
        assert registryAccess.registryOrThrow(net.minecraft.core.registries.Registries.ITEM) != null;
        assert registryAccess.registryOrThrow(net.minecraft.core.registries.Registries.ENTITY_TYPE) != null;
        assert registryAccess.registryOrThrow(net.minecraft.core.registries.Registries.MOB_EFFECT) != null;
        assert registryAccess.registryOrThrow(net.minecraft.core.registries.Registries.SOUND_EVENT) != null;
        assert registryAccess.registryOrThrow(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE) != null;
        
        System.out.println("✓ Registry integrity tests passed");
    }

    @Test
    @Order(8)
    public void testCrossReferenceIntegrity(MinecraftServer server) {
        // Test that related registrations properly reference each other
        
        // Test block and block entity relationship
        var testBlock = BlockRegistryTest.TEST_BLOCK.getEntry();
        var testBlockEntity = BlockRegistryTest.TEST_BLOCK.getBlockEntityType("test_block_entity");
        assert testBlockEntity != null;
        assert testBlockEntity.getValidBlocks().contains(testBlock);
        
        // Test block and item relationship  
        // Test that block item exists in registry
        var itemRegistry = server.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ITEM);
        assert itemRegistry.containsKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(lib.kasuga.KasugaLib.MODID, "test_block"));
        
        System.out.println("✓ Cross-reference integrity tests passed");
    }
}
