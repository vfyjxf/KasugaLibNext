package lib.kasuga.core.resource.pack;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public class ScopedVanillaFileResourcePack implements FlattenScopedPackResources {
    private final ZipFile zipFile;

    public ScopedVanillaFileResourcePack(ZipFile zipFile) {
        this.zipFile = zipFile;
    }

    @Override
    public InputStream open(String prefix, String path) throws IOException {
        return zipFile.getInputStream(zipFile.getEntry(wrapPrefix(prefix) + ScopedPackResources.firstSplash(path)));
    }

    @Override
    public boolean exists(String prefix, String path) {
        return zipFile.getEntry(wrapPrefix(prefix) + ScopedPackResources.firstSplash(path)) != null;
    }

    @Override
    public Stream<String> listEntries(String prefix) {
        return zipFile.stream()
                .filter(entry -> entry.getName().startsWith(wrapPrefix(prefix)))
                .map(entry -> entry.getName().substring(wrapPrefix(prefix).length()));
    }

    @Override
    public boolean isRegularFile(String prefix, String path) {
        return !zipFile.getEntry(wrapPrefix(prefix) + ScopedPackResources.firstSplash(path)).isDirectory();
    }

    @Override
    public boolean isDirectory(String prefix, String path) {
        return zipFile.getEntry(wrapPrefix(prefix) + ScopedPackResources.firstSplash(path)).isDirectory();
    }

    @Override
    public String getName() {
        return zipFile.getName();
    }

    private static String wrapPrefix(String prefix) {
        return prefix + (Objects.equals(prefix, "") ? "" : "/");
    }
}
