package lib.kasuga.scripting.discovery;

import jakarta.annotation.Nullable;

import java.util.List;

public record PackageInfo(
        @Nullable String name,
        @Nullable String engine,
        @Nullable String description,
        List<String> workspaces,
        EntryConfig entry
) {
    public record EntryConfig(
            List<String> server,
            List<String> client,
            List<String> common
    ) {}
}
