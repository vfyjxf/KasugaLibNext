package lib.kasuga.test.registration.minecraft.block;

import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibRegistry;
import lib.kasuga.registration.Registry;
import lib.kasuga.registration.minecraft.block.BlockReg;
import lib.kasuga.test.registration.minecraft.block_entity.TestBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

// With Neoforge's test server
@ExtendWith(EphemeralTestServerProvider.class)
public class BlockRegistryTest {
    public static Registry registry = KasugaLibRegistry.getRegistryOf(KasugaLib.MODID);
    public static BlockReg<TestBlock> TEST_BLOCK = BlockReg.of("test_block", TestBlock::new)
            .destroyTime(10)
            .noOcclusion()
            .withBlockEntity("test_block_entity", TestBlockEntity::new)
            .withDefaultBlockItem("test_block")
            .setParent(registry);

    @Test()
    public void testBlockRegistry(MinecraftServer server) {
        assert TEST_BLOCK.getEntry() != null;
        assert TEST_BLOCK.getBlockEntityType("test_block_entity") != null;
        assert TEST_BLOCK.getBlockEntityType().getValidBlocks().contains(TEST_BLOCK.getEntry());
        assert server.registryAccess().registryOrThrow(Registries.ITEM).containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_block"));
    }
}
