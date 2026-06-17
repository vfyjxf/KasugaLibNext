package lib.kasuga.slp.javet.module;

import jakarta.annotation.Nullable;
import lib.kasuga.scripting.module.*;

import java.util.ArrayList;
import java.util.List;

public class JsModuleResolver implements ModuleResolver {

    @Override
    @Nullable
    public ResolvedScript locateScript(ResolvedPackage pkg, List<String> relativeSegments) {
        List<String> segments = relativeSegments.isEmpty()
            ? (pkg.info().main() != null ? List.of(pkg.info().main()) : List.of())
            : relativeSegments;

        if (segments.isEmpty()) return null;

        String basePath = String.join("/", segments);

        List<String> candidates = new ArrayList<>();
        candidates.add(basePath + ".js");
        candidates.add(basePath + "/index.js");

        for (String candidate : candidates) {
            if (pkg.packResources().exists(pkg.packRelativeRoot(), candidate)) {
                return new ResolvedScript(candidate, pkg.packResources(), new ModuleId(segments), pkg);
            }
        }

        return null;
    }

    @Override
    public String getExtension() {
        return "js";
    }
}
