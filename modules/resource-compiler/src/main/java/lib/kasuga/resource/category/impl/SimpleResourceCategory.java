package lib.kasuga.resource.category.impl;

import lib.kasuga.resource.category.ResourceCategory;
import lib.kasuga.resource.model.SourceResource;
import lib.kasuga.resource.transformer.Transformer;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.function.BiFunction;

public class SimpleResourceCategory implements ResourceCategory {

    private final String folderName;
    private final BiFunction<String, Path, Path> targetResolver;
    private final TreeSet<Transformer> transformers;

    public SimpleResourceCategory(String folderName,
                                   BiFunction<String, Path, Path> targetResolver,
                                   Transformer... transformers) {
        this.folderName = folderName;
        this.targetResolver = targetResolver;
        this.transformers = new TreeSet<>(Arrays.asList(transformers));
    }

    @Override
    public String getFolderName() {
        return folderName;
    }

    @Override
    public Path getResourceTargetPath(SourceResource source) {
        return targetResolver.apply(source.namespace(), source.relativePath());
    }

    @Override
    public TreeSet<Transformer> getTransformers() {
        return new TreeSet<>(transformers);
    }
}
