package lib.kasuga.test.scripting;

import jakarta.annotation.Nullable;
import lib.kasuga.scripting.module.ModuleResolver;
import lib.kasuga.scripting.module.ResolvedPackage;
import lib.kasuga.scripting.module.ResolvedScript;
import lib.kasuga.scripting.module.ModuleId;

import java.util.ArrayList;
import java.util.List;

public class MockModuleResolver implements ModuleResolver {

    @Override
    @Nullable
    public ResolvedScript locateScript(ResolvedPackage pkg, List<String> relativeSegments) {
        List<String> segments = relativeSegments.isEmpty()
            ? (pkg.info().main() != null ? List.of(pkg.info().main()) : List.of())
            : relativeSegments;

        String basePath = String.join("/", segments);

        List<String> candidates = new ArrayList<>();
        candidates.add(basePath + ".mock");
        if (!segments.isEmpty()) {
            candidates.add(basePath + "/index.mock");
        }

        for (String candidate : candidates) {
            if (pkg.packResources().exists(pkg.packRelativeRoot(), candidate)) {
                return new ResolvedScript(candidate, pkg.packResources(), new ModuleId(segments), pkg);
            }
        }

        return null;
    }

    @Override
    public String getExtension() {
        return "mock";
    }
}
