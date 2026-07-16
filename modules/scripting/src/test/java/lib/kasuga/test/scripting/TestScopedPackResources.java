package lib.kasuga.test.scripting;

import lib.kasuga.core.resource.pack.HierarchicalScopedPackResources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TestScopedPackResources implements HierarchicalScopedPackResources {
    private final Path root;
    private final String name;

    public TestScopedPackResources(Path root, String name) {
        this.root = root;
        this.name = name;
    }

    @Override
    public InputStream open(String prefix, String path) throws IOException {
        return Files.newInputStream(resolve(prefix, path));
    }

    @Override
    public boolean exists(String prefix, String path) {
        return Files.exists(resolve(prefix, path));
    }

    @Override
    public boolean isRegularFile(String prefix, String path) {
        return Files.isRegularFile(resolve(prefix, path));
    }

    @Override
    public boolean isDirectory(String prefix, String path) {
        return Files.isDirectory(resolve(prefix, path));
    }

    @Override
    public List<String> list(String prefix, String path) throws IOException {
        try (var stream = Files.list(resolve(prefix, path))) {
            return stream.map(p -> p.getFileName().toString()).toList();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    private Path resolve(String prefix, String path) {
        if (path.startsWith("/"))
            path = path.substring(1);
        return root.resolve(prefix).resolve(path);
    }
}
