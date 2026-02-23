package lib.kasuga.resource;

import lib.kasuga.resource.compiler.ResourceProcessor;
import lib.kasuga.resource.compiler.ResourceScanner;
import lib.kasuga.resource.model.SourceResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ResourceCompilerMain {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        File fromFolder = new File(args[0]);
        File toFolder = new File(args[1]);
        String modId = args[2];

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/module.toml");

        Set<Path> allToml = Files.walk(fromFolder.toPath())
                .filter(matcher::matches)
                .collect(Collectors.toUnmodifiableSet());

        Set<Path> allParents = allToml.stream()
                .map(Path::getParent)
                .collect(Collectors.toUnmodifiableSet());

        // Filter to top-level module roots: no ancestor directory also contains module.toml
        Set<Path> topLevelModuleRoots = allToml.stream()
                .filter(p -> allParents.stream()
                        .noneMatch(u -> !u.equals(p.getParent()) && p.getParent().startsWith(u)))
                .map(Path::getParent)
                .collect(Collectors.toUnmodifiableSet());

        System.out.println("Found " + topLevelModuleRoots.size() + " module(s). Scanning resources...");

        Map<Path, Set<SourceResource>> grouped = ResourceScanner.scan(topLevelModuleRoots, modId);

        System.out.println("Grouped into " + grouped.size() + " target file(s). Processing...");

        ResourceProcessor.process(grouped, toFolder.toPath());

        System.out.println("Resource compilation complete. Output: " + toFolder.getAbsolutePath());
    }
}
