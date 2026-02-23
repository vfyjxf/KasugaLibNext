package lib.kasuga.resource.compiler;

import lib.kasuga.resource.model.LoadedResource;
import lib.kasuga.resource.model.SourceResource;
import lib.kasuga.resource.transformer.Transformer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

public class ResourceProcessor {

    public static void process(Map<Path, Set<SourceResource>> grouped, Path outputRoot)
            throws InterruptedException, ExecutionException {
        ForkJoinPool pool = ForkJoinPool.commonPool();

        List<ForkJoinTask<?>> tasks = grouped.entrySet().stream()
                .map(entry -> pool.submit(() -> processEntry(entry.getKey(), entry.getValue(), outputRoot)))
                .collect(Collectors.toList());

        List<Throwable> errors = new ArrayList<>();
        for (ForkJoinTask<?> task : tasks) {
            try {
                task.get();
            } catch (ExecutionException e) {
                errors.add(e.getCause() != null ? e.getCause() : e);
            }
        }

        if (!errors.isEmpty()) {
            errors.forEach(e -> System.err.println("[ERROR] " + e.getMessage()));
            throw new RuntimeException("Resource compilation failed with " + errors.size() + " error(s).");
        }
    }

    private static void processEntry(Path targetPath, Set<SourceResource> sources, Path outputRoot) {
        if (sources.isEmpty()) return;

        TreeSet<Transformer> transformers = sources.iterator().next().category().getTransformers();
        Path fullTarget = outputRoot.resolve(targetPath);

        // Pipeline: track the effective source set for each transformer's shouldTransform check
        Set<SourceResource> currentSources = new LinkedHashSet<>(sources);
        Set<LoadedResource> currentLoaded = null;
        boolean anyTransformApplied = false;

        for (Transformer transformer : transformers) {
            if (!transformer.shouldTransform(currentSources)) continue;

            // Lazy load: only read bytes on first transformer hit
            if (currentLoaded == null) {
                currentLoaded = sources.stream()
                        .map(sr -> {
                            try {
                                return new LoadedResource(sr, Files.readAllBytes(sr.file().toPath()));
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to read: " + sr.file(), e);
                            }
                        })
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }

            currentLoaded = transformer.transform(currentLoaded);
            anyTransformApplied = true;

            // Update effective sources for the next transformer's shouldTransform
            currentSources = currentLoaded.stream()
                    .map(LoadedResource::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        try {
            if (!anyTransformApplied) {
                if (sources.size() == 0) return;
                if (sources.size() == 1) {
                    SourceResource src = sources.iterator().next();
                    Files.createDirectories(fullTarget.getParent());
                    Files.copy(src.file().toPath(), fullTarget, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    throwConflict(sources, fullTarget);
                }
            } else {
                if (currentLoaded == null || currentLoaded.isEmpty()) return;
                if (currentLoaded.size() == 1) {
                    LoadedResource result = currentLoaded.iterator().next();
                    Files.createDirectories(fullTarget.getParent());
                    Files.write(fullTarget, result.data());
                } else {
                    throwConflict(sources, fullTarget);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write: " + fullTarget, e);
        }
    }

    private static void throwConflict(Set<SourceResource> sources, Path target) {
        String sourceFiles = sources.stream()
                .map(s -> s.file().toString())
                .collect(Collectors.joining(", "));
        throw new RuntimeException(
                "Unable to resolve the conflict. Source: [" + sourceFiles + "], Target: [" + target + "]");
    }
}
