package lib.kasuga.resource.category;

import lib.kasuga.resource.model.SourceResource;
import lib.kasuga.resource.transformer.Transformer;

import java.nio.file.Path;
import java.util.TreeSet;

public interface ResourceCategory {

    String getFolderName();

    Path getResourceTargetPath(SourceResource source);

    TreeSet<Transformer> getTransformers();
}
