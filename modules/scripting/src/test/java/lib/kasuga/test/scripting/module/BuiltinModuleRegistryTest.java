package lib.kasuga.test.scripting.module;

import lib.kasuga.KasugaLib;
import lib.kasuga.scripting.module.BuiltinModuleRegistry;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
public class BuiltinModuleRegistryTest {

    private BuiltinModuleRegistry registry;

    @BeforeEach
    void setUp() {
        registry = KasugaLib.getBean(BuiltinModuleRegistry.class);
        registry.clear();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldRegisterAndResolveWithCorrectValues(MinecraftServer server) {
        registry.register("console", () -> Map.of("log", "native", "level", 3));
        Object result = registry.resolve("console");
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals(2, map.size());
        assertEquals("native", map.get("log"));
        assertEquals(3, map.get("level"));
    }

    @Test
    public void shouldReturnNullForUnknown(MinecraftServer server) {
        assertNull(registry.resolve("nonexistent"));
        assertFalse(registry.has("nonexistent"));
    }

    @Test
    public void shouldResolveWithExactValues(MinecraftServer server) {
        registry.register("fs", () -> "builtin:fs");
        assertEquals("builtin:fs", registry.resolve("fs"));
    }

    @Test
    public void shouldListAllRegisteredNames(MinecraftServer server) {
        registry.register("a", () -> 1);
        registry.register("b", () -> 2);
        registry.register("c", () -> 3);
        var names = registry.names();
        assertEquals(3, names.size());
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));
        assertTrue(names.contains("c"));
        assertFalse(names.contains("d"));
    }

    @Test
    public void shouldCallSupplierOnEachResolveAndReturnFreshValue(MinecraftServer server) {
        AtomicInteger counter = new AtomicInteger(0);
        registry.register("counter", counter::incrementAndGet);

        assertEquals(1, registry.resolve("counter"));
        assertEquals(2, registry.resolve("counter"));
        assertEquals(3, registry.resolve("counter"));
        assertEquals(3, counter.get());
    }

    @Test
    public void shouldOverwritePreviousRegistration(MinecraftServer server) {
        registry.register("x", () -> "first");
        assertEquals("first", registry.resolve("x"));

        registry.register("x", () -> "second");
        assertEquals("second", registry.resolve("x"));
    }

    @Test
    public void shouldClearAll(MinecraftServer server) {
        registry.register("x", () -> 1);
        registry.register("y", () -> 2);
        registry.clear();
        assertFalse(registry.has("x"));
        assertFalse(registry.has("y"));
        assertNull(registry.resolve("x"));
        assertNull(registry.resolve("y"));
        assertTrue(registry.names().isEmpty());
    }
}
