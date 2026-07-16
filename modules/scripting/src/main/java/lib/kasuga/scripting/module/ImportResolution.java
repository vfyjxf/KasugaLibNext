package lib.kasuga.scripting.module;

import java.util.List;

public record ImportResolution(
    String packageName,
    List<String> relativePath
) {}
