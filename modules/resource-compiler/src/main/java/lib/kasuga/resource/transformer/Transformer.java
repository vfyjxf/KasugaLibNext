package lib.kasuga.resource.transformer;

import lib.kasuga.resource.model.LoadedResource;
import lib.kasuga.resource.model.SourceResource;

import java.util.Set;

public interface Transformer extends Comparable<Transformer> {

    int getPriority();

    boolean shouldTransform(Set<SourceResource> resources);

    Set<LoadedResource> transform(Set<LoadedResource> loadedResources);

    @Override
    default int compareTo(Transformer other) {
        int cmp = Integer.compare(this.getPriority(), other.getPriority());
        if (cmp != 0) return cmp;
        return this.getClass().getName().compareTo(other.getClass().getName());
    }
}
