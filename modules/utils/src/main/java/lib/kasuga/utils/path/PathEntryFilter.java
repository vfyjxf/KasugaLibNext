package lib.kasuga.utils.path;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class PathEntryFilter extends ArrayList<PathFilter> {

    public PathEntryFilter() {}

    public PathEntryFilter(Collection<PathFilter> filters) {
        super(filters);
    }
    @Override
    public String toString() {
        return this.stream().map(Object::toString).collect(Collectors.joining(File.pathSeparator));
    }
}
