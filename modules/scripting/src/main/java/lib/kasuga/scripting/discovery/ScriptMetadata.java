package lib.kasuga.scripting.discovery;

import java.util.List;

public record ScriptMetadata(
        List<String> requiredEngines,
        List<String> optionalEngines
) {}
