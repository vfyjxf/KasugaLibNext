package lib.kasuga.test.scripting.module;

import lib.kasuga.KasugaLib;
import lib.kasuga.scripting.ScriptEngineType;
import lib.kasuga.scripting.discovery.PackageInfo;
import lib.kasuga.scripting.module.PackageRegistry;
import lib.kasuga.scripting.module.ResolvedPackage;
import lib.kasuga.test.scripting.MockModuleResolver;
import lib.kasuga.test.scripting.MockScriptEngine;
import lib.kasuga.test.scripting.TestScopedPackResources;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
public class PackageRegistryTest {

    private PackageRegistry registry;
    private ScriptEngineType<MockScriptEngine> mockEngineType;

    @BeforeEach
    void setUp() {
        registry = KasugaLib.getBean(PackageRegistry.class);
        registry.clear();
        MockScriptEngine mockInstance = new MockScriptEngine();
        mockEngineType = ScriptEngineType.<MockScriptEngine>builder(() -> mockInstance)
            .scriptType("mock")
            .resolver(new MockModuleResolver())
            .build();
        mockInstance.setType(mockEngineType);
    }

    private ResolvedPackage createPackage(String name, String engine, String version,
                                           String main, String packRelativeRoot) {
        PackageInfo info = new PackageInfo(
            name, engine, null, version, main,
            List.of(),
            new PackageInfo.EntryConfig(List.of(), List.of(), List.of())
        );
        TestScopedPackResources pack = new TestScopedPackResources(Path.of("."), "test-pack");
        return new ResolvedPackage(info, pack, packRelativeRoot, mockEngineType);
    }

    @Test
    public void shouldRegisterAndLookupWithCorrectValues(MinecraftServer server) {
        ResolvedPackage pkg = createPackage("@test/math", "mock", "2.0.0", "index", "scripts");
        assertNull(registry.register(pkg));

        ResolvedPackage found = registry.lookup("@test/math");
        assertNotNull(found);
        assertEquals("@test/math", found.info().name());
        assertEquals("mock", found.info().engine());
        assertEquals("2.0.0", found.info().version());
        assertEquals("index", found.info().main());
        assertEquals("scripts", found.packRelativeRoot());
        assertEquals("mock", found.engine().scriptType);
    }

    @Test
    public void shouldRejectDuplicateNameWithErrorMessage(MinecraftServer server) {
        ResolvedPackage pkg1 = createPackage("@test/math", "mock", "1.0.0", "index", "scripts");
        ResolvedPackage pkg2 = createPackage("@test/math", "mock", "2.0.0", "main", "scripts/other");

        assertNull(registry.register(pkg1));
        String error = registry.register(pkg2);
        assertNotNull(error);
        assertEquals("Duplicate package name: @test/math (already registered from test-pack)", error);

        // First package should still be registered
        ResolvedPackage found = registry.lookup("@test/math");
        assertEquals("1.0.0", found.info().version());
        assertEquals("scripts", found.packRelativeRoot());
    }

    @Test
    public void shouldReturnNullForNonexistent(MinecraftServer server) {
        assertNull(registry.lookup("@test/nonexistent"));
        assertNull(registry.lookup(""));
        assertNull(registry.lookup("@other/missing"));
    }

    @Test
    public void shouldClearAllPackages(MinecraftServer server) {
        registry.register(createPackage("@test/a", "mock", "1.0.0", "index", "scripts/a"));
        registry.register(createPackage("@test/b", "mock", "2.0.0", "main", "scripts/b"));
        registry.clear();
        assertNull(registry.lookup("@test/a"));
        assertNull(registry.lookup("@test/b"));
    }

    @Test
    public void shouldPreserveAllRegisteredPackages(MinecraftServer server) {
        registry.register(createPackage("@test/x", "mock", "1.0.0", "index", "x"));
        registry.register(createPackage("@test/y", "mock", "2.0.0", "main", "y"));
        registry.register(createPackage("@test/z", "mock", "3.0.0", "entry", "z"));

        assertEquals("1.0.0", registry.lookup("@test/x").info().version());
        assertEquals("2.0.0", registry.lookup("@test/y").info().version());
        assertEquals("3.0.0", registry.lookup("@test/z").info().version());
        assertEquals("x", registry.lookup("@test/x").packRelativeRoot());
        assertEquals("y", registry.lookup("@test/y").packRelativeRoot());
        assertEquals("z", registry.lookup("@test/z").packRelativeRoot());
    }
}
