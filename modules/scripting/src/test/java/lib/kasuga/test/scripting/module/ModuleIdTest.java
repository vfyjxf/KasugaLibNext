package lib.kasuga.test.scripting.module;

import lib.kasuga.scripting.module.ModuleId;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
public class ModuleIdTest {

    @Test
    public void shouldConstructFromSegments() {
        ModuleId id = new ModuleId(List.of("math", "utils"));
        assertEquals("math/utils", id.toPath());
        assertEquals(List.of("math", "utils"), id.segments());
    }

    @Test
    public void shouldDetectPackageRoot() {
        ModuleId root = new ModuleId(List.of());
        assertTrue(root.isPackageRoot());
        assertEquals("", root.toPath());
    }

    @Test
    public void shouldNotBeRootWhenSegmentsExist() {
        ModuleId id = new ModuleId(List.of("foo"));
        assertFalse(id.isPackageRoot());
    }

    @Test
    public void shouldHandleSingleSegment() {
        ModuleId id = new ModuleId(List.of("index"));
        assertEquals("index", id.toPath());
    }
}
