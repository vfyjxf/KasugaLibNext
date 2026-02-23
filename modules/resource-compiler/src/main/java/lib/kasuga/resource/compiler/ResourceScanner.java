package lib.kasuga.resource.compiler;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import lib.kasuga.resource.category.AllResourceCategory;
import lib.kasuga.resource.category.ResourceCategory;
import lib.kasuga.resource.model.SourceResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceScanner {

    public static Map<Path, Set<SourceResource>> scan(Set<Path> moduleRoots, String modId)
            throws ExecutionException, InterruptedException {
        ForkJoinPool pool = ForkJoinPool.commonPool();

        List<ForkJoinTask<List<SourceResource>>> tasks = moduleRoots.stream()
                .map(root -> pool.submit(() -> scanModule(root, modId)))
                .collect(Collectors.toList());

        Map<Path, Set<SourceResource>> result = new LinkedHashMap<>();
        for (ForkJoinTask<List<SourceResource>> task : tasks) {
            for (SourceResource sr : task.get()) {
                Path targetPath = sr.category().getResourceTargetPath(sr);
                result.computeIfAbsent(targetPath, k -> new LinkedHashSet<>()).add(sr);
            }
        }
        return result;
    }

    private static List<SourceResource> scanModule(Path moduleRoot, String modId) throws IOException {
        // Load module.toml if present (reserved for future config fields)
        Path tomlPath = moduleRoot.resolve("module.toml");
        if (Files.exists(tomlPath)) {
            try (CommentedFileConfig config = CommentedFileConfig.of(tomlPath)) {
                config.load();
            }
        }

        List<SourceResource> resources = new ArrayList<>();

        try (Stream<Path> walk = Files.walk(moduleRoot)) {
            List<Path> files = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().equals("module.toml"))
                    .collect(Collectors.toList());

            for (Path filePath : files) {
                Path relativeToModule = moduleRoot.relativize(filePath);
                if (relativeToModule.getNameCount() < 2) continue;

                String categoryName = relativeToModule.getName(0).toString();
                ResourceCategory category = AllResourceCategory.get(categoryName);
                if (category == null) {
                    throw new RuntimeException("Unable to found category for \"" + categoryName + "\"");
                }

                Path relPath = relativeToModule.subpath(1, relativeToModule.getNameCount());
                resources.add(new SourceResource(category, relPath, filePath.toFile(), modId));
            }
        }

        return resources;
    }
}
