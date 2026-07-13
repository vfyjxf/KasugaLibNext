package lib.kasuga.test.scripting.discovery;

import lib.kasuga.scripting.discovery.PackageInfo;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
public class PackageInfoTest {

    @Test
    public void shouldParseFullToml() {
        PackageInfo info = new PackageInfo(
            "@test/mock-pack",
            "mock",
            "A test package",
            "1.0.0",
            "index",
            List.of("packages/*"),
            new PackageInfo.EntryConfig(
                List.of("server-entry"),
                List.of("client-entry"),
                List.of("common-entry")
            )
        );

        assertEquals("@test/mock-pack", info.name());
        assertEquals("mock", info.engine());
        assertEquals("A test package", info.description());
        assertEquals("1.0.0", info.version());
        assertEquals("index", info.main());
        assertEquals(List.of("packages/*"), info.workspaces());
        assertEquals(List.of("server-entry"), info.entry().server());
        assertEquals(List.of("client-entry"), info.entry().client());
        assertEquals(List.of("common-entry"), info.entry().common());
    }

    @Test
    public void shouldHandleOptionalFields() {
        PackageInfo info = new PackageInfo(
            "@test/minimal",
            "mock",
            null,
            null,
            null,
            List.of(),
            new PackageInfo.EntryConfig(List.of(), List.of(), List.of())
        );

        assertEquals("@test/minimal", info.name());
        assertEquals("mock", info.engine());
        assertNull(info.description());
        assertNull(info.version());
        assertNull(info.main());
        assertTrue(info.workspaces().isEmpty());
        assertTrue(info.entry().server().isEmpty());
        assertTrue(info.entry().client().isEmpty());
        assertTrue(info.entry().common().isEmpty());
    }

    @Test
    public void shouldAllowNullName() {
        PackageInfo info = new PackageInfo(
            null,
            "mock",
            null,
            null,
            null,
            List.of(),
            new PackageInfo.EntryConfig(List.of(), List.of(), List.of())
        );

        assertNull(info.name());
    }
}
