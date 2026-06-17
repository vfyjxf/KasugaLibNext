package lib.kasuga.test.scripting.discovery;

import lib.kasuga.scripting.ScriptEngineType;
import lib.kasuga.scripting.discovery.PackageInfo;
import lib.kasuga.scripting.discovery.ScriptPackage;
import lib.kasuga.scripting.module.ResolvedPackage;
import lib.kasuga.test.scripting.MockModuleResolver;
import lib.kasuga.test.scripting.MockScriptEngine;
import lib.kasuga.test.scripting.TestScopedPackResources;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
public class ScriptPackageTest {

    private ScriptEngineType<MockScriptEngine> createMockEngineType() {
        MockScriptEngine mockInstance = new MockScriptEngine();
        ScriptEngineType<MockScriptEngine> type = ScriptEngineType.<MockScriptEngine>builder(() -> mockInstance)
            .scriptType("mock")
            .resolver(new MockModuleResolver())
            .build();
        mockInstance.setType(type);
        return type;
    }

    private ResolvedPackage createResolved(String name, String packRelativeRoot, List<String> commonEntries) {
        PackageInfo info = new PackageInfo(
            name, "mock", null, "1.0.0", "index",
            List.of(),
            new PackageInfo.EntryConfig(List.of(), List.of(), commonEntries)
        );
        TestScopedPackResources pack = new TestScopedPackResources(Path.of("."), "test-pack");
        return new ResolvedPackage(info, pack, packRelativeRoot, createMockEngineType());
    }

    @Test
    public void shouldBuildTreeWithCorrectStructure() {
        ScriptPackage root = new ScriptPackage();
        ScriptPackage child1 = new ScriptPackage(createResolved("@test/a", "scripts/a", List.of()));
        ScriptPackage child2 = new ScriptPackage(createResolved("@test/b", "scripts/b", List.of()));

        root.addChild(child1);
        root.addChild(child2);

        assertEquals(2, root.children().size());
        assertSame(child1, root.children().get(0));
        assertSame(child2, root.children().get(1));

        // Root is anonymous
        assertNull(root.info());
        assertNull(root.getResolved());
        assertNull(root.engine());

        // Children have correct names
        assertEquals("@test/a", root.children().get(0).info().name());
        assertEquals("@test/b", root.children().get(1).info().name());
    }

    @Test
    public void shouldExposeInfoWithCorrectValues() {
        ResolvedPackage resolved = createResolved("@test/math", "scripts", List.of("index"));
        ScriptPackage pkg = new ScriptPackage(resolved);

        assertEquals("@test/math", pkg.info().name());
        assertEquals("mock", pkg.info().engine());
        assertEquals("1.0.0", pkg.info().version());
        assertEquals("index", pkg.info().main());
        assertEquals("mock", pkg.engine().scriptType);
        assertEquals("scripts", pkg.getResolved().packRelativeRoot());
    }

    @Test
    public void shouldHandleAnonymousPackageWithNullFields() {
        ScriptPackage pkg = new ScriptPackage();
        assertNull(pkg.getResolved());
        assertNull(pkg.info());
        assertNull(pkg.engine());
        assertTrue(pkg.children().isEmpty());
    }

    @Test
    public void shouldHaveAllEntryTypesWithCorrectNames() {
        PackageInfo info = new PackageInfo(
            "@test/entries", "mock", null, "1.0.0", "index",
            List.of(),
            new PackageInfo.EntryConfig(
                List.of("server-init"),
                List.of("client-init"),
                List.of("common-init", "common-second")
            )
        );
        TestScopedPackResources pack = new TestScopedPackResources(Path.of("."), "test-pack");
        ResolvedPackage resolved = new ResolvedPackage(info, pack, "scripts", createMockEngineType());

        ScriptPackage pkg = new ScriptPackage(resolved);
        assertEquals(List.of("server-init"), pkg.info().entry().server());
        assertEquals(List.of("client-init"), pkg.info().entry().client());
        assertEquals(List.of("common-init", "common-second"), pkg.info().entry().common());
        assertEquals(4, pkg.info().entry().server().size()
            + pkg.info().entry().client().size()
            + pkg.info().entry().common().size());
    }
}
