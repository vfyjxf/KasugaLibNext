package lib.kasuga.resource.model;

import lib.kasuga.resource.category.ResourceCategory;

import java.io.File;
import java.nio.file.Path;

public record SourceResource(ResourceCategory category, Path relativePath, File file, String namespace) {}
