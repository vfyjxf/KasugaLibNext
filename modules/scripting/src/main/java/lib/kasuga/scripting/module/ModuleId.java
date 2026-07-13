package lib.kasuga.scripting.module;

import java.util.List;

public record ModuleId(List<String> segments) {

    public String toPath() {
        return String.join("/", segments);
    }

    public boolean isPackageRoot() {
        return segments.isEmpty();
    }
}
