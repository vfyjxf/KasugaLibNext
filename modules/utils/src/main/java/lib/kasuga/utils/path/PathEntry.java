package lib.kasuga.utils.path;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class PathEntry extends ArrayList<String> {

    protected static Pattern p = Pattern.compile("[\\\\/]");
    public PathEntry(PathEntry parent) {
        super(parent);
    }
    public PathEntry(List<String> parent) {
        super(parent);
    }
    public PathEntry() {
        super();
    }

    public static PathEntry parse(String path, String separator) {
        PathEntry entry = new PathEntry();
        entry.addAll(Arrays.asList(path.split(separator)));
        return entry;
    }

    public static PathEntry parse(String path) {
        // Use operating system file separator as default (File.separator)
        return parse(path, File.separator);
    }

    public static PathEntry parseAny(String path) {
        return parse(path, "[\\\\/]");
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    public PathEntry extend(String subPath) {
        PathEntry extended = new PathEntry();
        extended.addAll(this);
        extended.addAll(Arrays.asList(p.split(subPath)));
        return extended;
    }

    @Override
    public String toString() {
        return String.join(File.separator, this);
    }

    public PathEntry getParent() {
        return new PathEntry(subList(0, size() - 1));
    }
}
