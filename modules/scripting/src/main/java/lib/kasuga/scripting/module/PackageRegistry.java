package lib.kasuga.scripting.module;

import io.micronaut.context.annotation.Context;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

@Context()
public class PackageRegistry {

    private final Map<String, ResolvedPackage> packages = new HashMap<>();

    @Nullable
    public String register(ResolvedPackage pkg) {
        String name = pkg.info().name();
        if (packages.containsKey(name)) {
            return "Duplicate package name: " + name + " (already registered from "
                + packages.get(name).packResources().getName() + ")";
        }
        packages.put(name, pkg);
        return null;
    }

    @Nullable
    public ResolvedPackage lookup(String packageName) {
        return packages.get(packageName);
    }

    public Map<String, ResolvedPackage> all() {
        return Map.copyOf(packages);
    }

    public void clear() {
        packages.clear();
    }
}
