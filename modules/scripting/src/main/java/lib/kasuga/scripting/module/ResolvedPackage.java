package lib.kasuga.scripting.module;

import lib.kasuga.core.resource.pack.ScopedPackResources;
import lib.kasuga.scripting.ScriptEngineType;
import lib.kasuga.scripting.discovery.PackageInfo;

public record ResolvedPackage(
    PackageInfo info,
    ScopedPackResources packResources,
    String packRelativeRoot,
    ScriptEngineType<?> engine
) {}
