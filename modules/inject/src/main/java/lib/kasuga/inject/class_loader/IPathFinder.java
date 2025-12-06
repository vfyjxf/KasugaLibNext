package lib.kasuga.inject.class_loader;

import java.nio.file.Path;

@FunctionalInterface
public interface IPathFinder {
    public Path find(String path);
}
