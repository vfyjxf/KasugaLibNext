package lib.kasuga.test.registration.minecraft.block_entity;

import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibRegistry;
import lib.kasuga.registration.Registry;
import lib.kasuga.registration.minecraft.block_entity.BlockEntityReg;
import lib.kasuga.test.registration.minecraft.block.BlockRegistryTest;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class BlockEntityRegistryTest {
    
    public static Registry registry = KasugaLibRegistry.getRegistryOf(KasugaLib.MODID);
    
    public static BlockEntityReg<TestCustomBlockEntity> TEST_CUSTOM_BLOCK_ENTITY =
            BlockEntityReg.of("test_custom_block_entity", TestCustomBlockEntity::new)
            .validBlocks(() -> BlockRegistryTest.TEST_BLOCK.getEntry())
            .setParent(registry);

    @Test
    public void testBlockEntityRegistry(MinecraftServer server) {
        // Test that block entity is registered
        assert TEST_CUSTOM_BLOCK_ENTITY.getEntry() != null;
        
        // Test registry contains the block entity
        var blockEntityRegistry = server.registryAccess().registryOrThrow(Registries.BLOCK_ENTITY_TYPE);
        assert blockEntityRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_custom_block_entity"));
        
        // Test block entity type properties
        var blockEntityType = TEST_CUSTOM_BLOCK_ENTITY.getEntry();
        assert blockEntityType.getValidBlocks().contains(BlockRegistryTest.TEST_BLOCK.getEntry());
    }
}
