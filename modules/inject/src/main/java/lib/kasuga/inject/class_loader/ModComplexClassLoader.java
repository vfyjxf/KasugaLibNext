package lib.kasuga.inject.class_loader;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;

public class ModComplexClassLoader extends ClassLoader {
    private final Function<String, Path> pathFinder;

    public ModComplexClassLoader(ClassLoader superLoader, Function<String, Path> pathFinder) {
        super(superLoader);
        this.pathFinder = pathFinder;
    }

    @Override
    public String getName() {
        return "ModComplexClassLoader";
    }

    @Nullable
    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if(name.startsWith("META-INF/micronaut") || name.startsWith("/META-INF/micronaut")) {
            return this.readResourcesRedirected(name);
        }
        return super.getResources(name);
    }

    protected Enumeration<URL> readResourcesRedirected(String name) throws IOException {
        return Collections.enumeration(List.of(pathFinder.apply(name).toUri().toURL()));
    }

    @Nullable
    @Override
    public InputStream getResourceAsStream(String name) {
        return super.getResourceAsStream(name);
    }
}
