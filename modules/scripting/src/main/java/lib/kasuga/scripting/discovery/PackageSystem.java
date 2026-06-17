package lib.kasuga.scripting.discovery;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import io.micronaut.context.annotation.Context;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import lib.kasuga.core.resource.pack.HierarchicalScopedPackResources;
import lib.kasuga.core.resource.pack.ScopedPackResources;
import lib.kasuga.scripting.ScriptEngineRegistry;
import lib.kasuga.scripting.ScriptEngineType;
import lib.kasuga.scripting.module.PackageRegistry;
import lib.kasuga.scripting.module.ResolvedPackage;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Context()
public class PackageSystem {

    @Inject()
    ScriptEngineRegistry engineRegistry;

    @Inject()
    PackageRegistry packageRegistry;

    protected final List<PackageLoadingError> loadingErrors = new ArrayList<>();

    @Getter
    protected final Map<String, List<Throwable>> engineErrors = new ConcurrentHashMap<>();

    protected final TomlParser scriptInfoParser = new TomlParser();

    protected final Set<String> engineMissing = new HashSet<>();


    public void init() {
        loadingErrors.clear();
        engineErrors.clear();
        engineMissing.clear();
    }

    public boolean hasLoadingErrors() {
        synchronized (loadingErrors) {
            return !loadingErrors.isEmpty();
        }
    }

    public List<PackageLoadingError> getLoadingErrors() {
        synchronized (loadingErrors) {
            return List.copyOf(loadingErrors);
        }
    }

    public void addLoadingError(PackageLoadingError error) {
        synchronized (loadingErrors) {
            loadingErrors.add(error);
        }
    }

    @Nullable
    public PackageInfo readPackageInfo(ScopedPackResources spr, String packRelativeRoot) {
        if (!spr.exists(packRelativeRoot, "package.toml"))
            return null;
        CommentedConfig config;
        try {
            config = scriptInfoParser.parse(new InputStreamReader(spr.open(packRelativeRoot, "package.toml")));
        } catch (IOException e) {
            throw new PackageLoadingError(e);
        }

        return new PackageInfo(
                config.get("name"),
                config.get("engine"),
                config.get("description"),
                config.get("version"),
                config.get("main"),
                formatEngineObj(config.get("workspaces")),
                new PackageInfo.EntryConfig(
                        formatEngineObj(config.get("entry.server")),
                        formatEngineObj(config.get("entry.client")),
                        formatEngineObj(config.get("entry.common"))
                )
        );
    }

    public List<ResolvedPackage> scan(ScopedPackResources packResource) {
        List<ResolvedPackage> result = new ArrayList<>();

        PackageInfo rootInfo = readPackageInfo(packResource, "scripts");
        if (rootInfo == null) return result;

        ScriptEngineType<?> rootEngine = resolveEngine(rootInfo.engine());
        if (rootEngine == null) return result;

        ResolvedPackage rootPkg = new ResolvedPackage(rootInfo, packResource, "scripts", rootEngine);
        String error = packageRegistry.register(rootPkg);
        if (error != null) {
            addLoadingError(new PackageLoadingError(error));
        } else {
            result.add(rootPkg);
        }

        for (String workspaceGlob : rootInfo.workspaces()) {
            List<ResolvedPackage> subPackages = scanWorkspace(packResource, "scripts", workspaceGlob);
            result.addAll(subPackages);
        }

        return result;
    }

    private List<ResolvedPackage> scanWorkspace(ScopedPackResources packResource, String prefix, String workspaceGlob) {
        List<ResolvedPackage> result = new ArrayList<>();

        if (!(packResource instanceof HierarchicalScopedPackResources hierarchical)) {
            return result;
        }

        String globPattern = workspaceGlob.endsWith("/*")
            ? workspaceGlob.substring(0, workspaceGlob.length() - 2)
            : workspaceGlob;

        try {
            List<String> entries = hierarchical.list(prefix, globPattern);
            for (String entry : entries) {
                String subPath = globPattern + "/" + entry;
                if (!hierarchical.isDirectory(prefix, subPath)) continue;

                PackageInfo subInfo = readPackageInfo(packResource, prefix + "/" + subPath);
                if (subInfo == null) continue;

                ScriptEngineType<?> subEngine = resolveEngine(subInfo.engine());
                if (subEngine == null) continue;

                ResolvedPackage subPkg = new ResolvedPackage(subInfo, packResource, prefix + "/" + subPath, subEngine);
                String error = packageRegistry.register(subPkg);
                if (error != null) {
                    addLoadingError(new PackageLoadingError(error));
                } else {
                    result.add(subPkg);
                }
            }
        } catch (IOException e) {
            addLoadingError(new PackageLoadingError(e));
        }

        return result;
    }

    @Nullable
    private ScriptEngineType<?> resolveEngine(@Nullable String engineName) {
        if (engineName == null) return null;
        ScriptEngineType<?> type = engineRegistry.resolve(engineName, true);
        if (type == null) {
            synchronized (engineMissing) { engineMissing.add(engineName); }
        } else if (!type.loadingIssues.isEmpty()) {
            engineErrors.put(engineName, List.copyOf(type.loadingIssues));
            synchronized (engineMissing) { engineMissing.add(engineName); }
            return null;
        }
        return type;
    }

    public ScriptMetadata readMetadata(ScopedPackResources spr) {
        if(!spr.exists("scripts", "script-info.toml"))
            return null;
        CommentedConfig metaConfig = null;
        try {
            metaConfig = scriptInfoParser.parse(new InputStreamReader(spr.open("scripts", "script-info.toml")));
        } catch (IOException e) {
            throw new PackageLoadingError(e);
        }


        return new ScriptMetadata(
                formatEngineObj(metaConfig.get("engines.required")),
                formatEngineObj(metaConfig.get("engines.recommended"))
        );
    }

    public List<ScriptEngineType<?>> ensureEngineRequirement(ScriptMetadata metadata) {
        List<ScriptEngineType<?>> engines = new ArrayList<>();
        for (String requiredEngine : metadata.requiredEngines()) {
            ScriptEngineType<?> engineType = engineRegistry.resolve(requiredEngine, true);
            if (engineType == null) {
                synchronized (engineMissing) {
                    engineMissing.add(requiredEngine);
                }
                continue;
            }

            if(!engineType.loadingIssues.isEmpty()) {
                engineErrors.put(requiredEngine, List.copyOf(engineType.loadingIssues));
                engineMissing.add(requiredEngine);
                continue;
            }

            engines.add(engineType);

        }
        return engines;
    }

    public Set<String> getMissingEngines() {
        synchronized (engineMissing) {
            return Set.copyOf(engineMissing);
        }
    }

    protected static List<String> formatEngineObj(Object origin) {
        if(origin == null)
            return List.of();
        if(origin instanceof String str)
            return List.of(str);
        if(origin instanceof Collection<?> coll) {
            return coll.stream().map(PackageSystem::formatEngineObj).flatMap(Collection::stream).toList();
        }
        return List.of(origin.toString());
    }
}
