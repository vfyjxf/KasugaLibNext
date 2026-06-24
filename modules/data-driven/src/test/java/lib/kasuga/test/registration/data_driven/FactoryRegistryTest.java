package lib.kasuga.test.registration.data_driven;

import lib.kasuga.registration.Reg;
import lib.kasuga.registration.factory.FactoryRegistry;
import net.minecraft.world.level.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FactoryRegistryTest {

    @Test
    void registerAndGet() {
        FactoryRegistry.BlockFactory factory = id -> null;
        FactoryRegistry.register("_test_dummy", factory);
        assertTrue(FactoryRegistry.contains("_test_dummy"));
        assertSame(factory, FactoryRegistry.get("_test_dummy"));
    }

    @Test
    void unknownTypeReturnsFalse() {
        assertFalse(FactoryRegistry.contains("_test_nonexistent_" + System.nanoTime()));
    }

    @Test
    void unknownTypeReturnsNull() {
        assertNull(FactoryRegistry.get("_test_nonexistent_" + System.nanoTime()));
    }

    @Test
    void registerDuplicateOverwrites() {
        FactoryRegistry.BlockFactory first = id -> null;
        FactoryRegistry.BlockFactory second = id -> null;
        FactoryRegistry.register("_test_overwrite", first);
        FactoryRegistry.register("_test_overwrite", second);
        assertSame(second, FactoryRegistry.get("_test_overwrite"));
    }

    @Test
    void factoryCreatesRegInstance() {
        // Requires NeoForge unitTest classpath for BlockReg
        FactoryRegistry.BlockFactory factory = FactoryRegistry.get("simple_block");
        assertNotNull(factory, "Expected 'simple_block' factory to be registered by DataDrivenTestFactories");
        Reg<?, Block> reg = factory.create("test_block_from_factory");
        assertNotNull(reg);
    }
}
