package lib.kasuga.test.scripting;

import lib.kasuga.scripting.ScriptEngineType;
import lib.kasuga.scripting.discovery.PackageSystem;
import lib.kasuga.scripting.module.PackageRegistry;
import lib.kasuga.scripting.module.ResolvedPackage;
import lib.kasuga.KasugaLib;
import lib.kasuga.scripting.ScriptEngineRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
public class ScriptRuntimeTest {

    private PackageSystem packageSystem;
    private PackageRegistry packageRegistry;
    private ScriptEngineRegistry engineRegistry;

    @BeforeEach
    void setUp() {
        packageSystem = KasugaLib.getBean(PackageSystem.class);
        packageRegistry = KasugaLib.getBean(PackageRegistry.class);
        engineRegistry = KasugaLib.getBean(ScriptEngineRegistry.class);
        packageSystem.init();
        packageRegistry.clear();

        MockScriptEngine mockInstance = new MockScriptEngine();
        ScriptEngineType<MockScriptEngine> mockEngine = ScriptEngineType.<MockScriptEngine>builder(() -> mockInstance)
            .scriptType("mock")
            .resolver(new MockModuleResolver())
            .build();
        mockInstance.setType(mockEngine);

        engineRegistry.register(
            ResourceLocation.fromNamespaceAndPath("test", "mock"),
            List.of("mock"),
            mockEngine,
            0
        );
    }

    private TestScopedPackResources loadTestPack(String packName) throws URISyntaxException {
        Path packPath = Path.of(getClass().getResource("/packs/" + packName).toURI());
        return new TestScopedPackResources(packPath, packName);
    }

    @Test
    public void shouldScanAndRegisterPackages(MinecraftServer server) throws URISyntaxException {
        TestScopedPackResources pack = loadTestPack("mock_pack");
        List<ResolvedPackage> discovered = packageSystem.scan(pack);

        assertFalse(discovered.isEmpty());
        assertNotNull(packageRegistry.lookup("@test/mock-pack"));
        assertNotNull(packageRegistry.lookup("@test/math"));
    }

    @Test
    public void shouldSkipPackWithoutToml(MinecraftServer server) throws URISyntaxException {
        TestScopedPackResources pack = loadTestPack("no_toml_pack");
        List<ResolvedPackage> discovered = packageSystem.scan(pack);
        assertTrue(discovered.isEmpty());
    }

    @Test
    public void shouldHandleScopedPackages(MinecraftServer server) throws URISyntaxException {
        TestScopedPackResources pack = loadTestPack("scoped_pack");
        List<ResolvedPackage> discovered = packageSystem.scan(pack);

        assertNotNull(packageRegistry.lookup("@scope/alpha"));
        assertNotNull(packageRegistry.lookup("@scope/beta"));
    }

    @Test
    public void shouldResolveEngine(MinecraftServer server) throws URISyntaxException {
        TestScopedPackResources pack = loadTestPack("mock_pack");
        List<ResolvedPackage> discovered = packageSystem.scan(pack);

        for (ResolvedPackage pkg : discovered) {
            assertNotNull(pkg.engine());
            assertEquals("mock", pkg.engine().scriptType);
        }
    }
}
