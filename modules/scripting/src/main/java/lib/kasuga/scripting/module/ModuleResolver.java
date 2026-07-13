package lib.kasuga.scripting.module;

import jakarta.annotation.Nullable;

import java.util.List;

public interface ModuleResolver {

    @Nullable
    ResolvedScript locateScript(ResolvedPackage pkg, List<String> relativeSegments);

    String getExtension();
}
