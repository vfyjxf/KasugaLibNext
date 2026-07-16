package lib.kasuga.test.registration.minecraft.block;

import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibRegistry;
import lib.kasuga.registration.Registry;
import lib.kasuga.registration.minecraft.block.BlockReg;
import lib.kasuga.registration.minecraft.block.BlockRegModifiers;
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
            .configure(BlockRegModifiers.BlockProperties.of("destroyTime", p -> { p.destroyTime(10); return p; }))
            .configure(BlockRegModifiers.BlockProperties.of("noOcclusion", p -> { p.noOcclusion(); return p; }))
            .withDefaultBlockItem("test_block")
            .setParent(registry);

    @Test()
    public void testBlockRegistry(MinecraftServer server) {
        assert TEST_BLOCK.getEntry() != null;
        assert server.registryAccess().registryOrThrow(Registries.ITEM).containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_block"));
    }
}
