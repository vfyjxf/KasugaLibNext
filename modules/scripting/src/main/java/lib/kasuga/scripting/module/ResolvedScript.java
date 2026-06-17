package lib.kasuga.scripting.module;

import lib.kasuga.core.resource.pack.ScopedPackResources;

import java.io.IOException;
import java.io.InputStream;

public record ResolvedScript(
    String filePath,
    ScopedPackResources pack,
    ModuleId moduleId,
    ResolvedPackage owner
) {
    public InputStream open() throws IOException {
        return pack.open(owner.packRelativeRoot(), filePath);
    }
}
