package lib.kasuga.scripting.discovery;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import io.micronaut.context.annotation.Context;
import jakarta.inject.Inject;
import lib.kasuga.core.resource.pack.ScopedPackResources;
import lib.kasuga.scripting.ScriptEngineRegistry;
import lib.kasuga.scripting.ScriptEngineType;
import lib.kasuga.structure.Pair;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Context()
public class PackageSystem {

    @Inject()
    ScriptEngineRegistry engineRegistry;

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

    public List<ScriptPackage> resolve(ScopedPackResources packResource, List<ScriptEngineType<?>> engines) {
        List<ScriptPackage> packages = new ArrayList<>();
        for (ScriptEngineType<?> engine : engines) {
            ScriptPackage root = new ScriptPackage();
            // List<ScriptPackage> enginePackageList = engine.resolver.resolvePackage(packResource);
//            for (ScriptPackage scriptPackage : enginePackageList) {
//                root.addChild(scriptPackage);
//            }
            packages.add(root);
        }
        return packages;
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

//    public PackageInfo readPackageInfo(ScopedPa1ckResources spr) {
//        if (!spr.exists("scripts", "package.toml"))
//            return null;
//        CommentedConfig config;
//        try {
//            config = scriptInfoParser.parse(new InputStreamReader(spr.open("scripts", "package.toml")));
//        } catch (IOException e) {
//            throw new PackageLoadingError(e);
//        }
//
//        return new PackageInfo(
//                config.get("name"),
//                config.get("engine"),
//                config.get("description"),
//                formatEngineObj(config.get("workspaces")),
//                new PackageInfo.EntryConfig(
//                        formatEngineObj(config.get("entry.server")),
//                        formatEngineObj(config.get("entry.client")),
//                        formatEngineObj(config.get("entry.common"))
//                )
//        );
//    }

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
}
