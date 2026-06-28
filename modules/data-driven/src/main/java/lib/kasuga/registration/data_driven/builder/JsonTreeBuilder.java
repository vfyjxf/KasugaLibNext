package lib.kasuga.registration.data_driven.builder;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import lib.kasuga.registration.data_driven.TypeHandler;
import lib.kasuga.registration.data_driven.TypeHandlerRegistry;
import lib.kasuga.registration.data_driven.context.BuildContext;
import lib.kasuga.registration.data_driven.context.JsonRegistryGroup;
import lib.kasuga.registration.data_driven.context.RegBuildContext;
import lib.kasuga.registration.data_driven.handler.BlockEntityTypeHandler;
import lib.kasuga.registration.data_driven.handler.BlockTypeHandler;
import lib.kasuga.registration.data_driven.handler.RegistryGroupHandler;
import lib.kasuga.registration.data_driven.handler.ItemTypeHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class JsonTreeBuilder {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static boolean handlersRegistered = false;

    private static synchronized void ensureHandlersRegistered() {
        if (handlersRegistered) return;
        handlersRegistered = true;
        TypeHandlerRegistry.register(new RegistryGroupHandler());
        TypeHandlerRegistry.register(new BlockTypeHandler());
        TypeHandlerRegistry.register(new ItemTypeHandler());
        TypeHandlerRegistry.register(new BlockEntityTypeHandler());
    }

    public static JsonRegistryGroup buildForMod(String modId) {
        ensureHandlersRegistered();

        LOGGER.info("[buildForMod] called for modId='{}'", modId);
        Path kasugalibDir = findKasugalibDir(modId);
        if (kasugalibDir == null) return null;

        JsonRegistryGroup rootGroup = new JsonRegistryGroup(modId + ":json_root");
        rootGroup.withProperty(ResourceLocation.class,
            loc -> ResourceLocation.fromNamespaceAndPath(modId, loc.getPath()));
        RegBuildContext context = new RegBuildContext(modId, rootGroup);

        // Pass 1: parse all JSON files
        Map<TypeHandler<?>, List<?>> parsed = new LinkedHashMap<>();
        try (Stream<Path> files = Files.list(kasugalibDir)) {
            files.filter(p -> p.toString().endsWith(".json"))
                 .sorted()
                 .forEach(path -> parseFile(path, parsed));
        } catch (IOException e) {
            LOGGER.error("Error scanning directory: {}", kasugalibDir, e);
        }

        if (parsed.isEmpty()) return null;

        int totalEntries = parsed.values().stream().mapToInt(List::size).sum();
        LOGGER.info("Building JSON registration tree for mod '{}' with {} entries across {} types",
                modId, totalEntries, parsed.size());

        // Pass 2: apply by phase
        List<TypeHandler<?>> ordered = TypeHandlerRegistry.all().stream()
            .sorted(Comparator.comparingInt(TypeHandler::getPhase))
            .toList();

        for (TypeHandler<?> handler : ordered) {
            List<?> defs = parsed.getOrDefault(handler, List.of());
            if (defs.isEmpty()) continue;
            LOGGER.info("[buildForMod] applying {} entries for type '{}' (phase {})",
                    defs.size(), handler.getTypeName(), handler.getPhase());
            for (Object def : defs) {
                applyUnchecked(handler, def, context);
            }
        }

        return rootGroup;
    }

    public static Map<String, JsonRegistryGroup> buildAll() {
        Set<Path> visitedRoots = new HashSet<>();
        Map<String, JsonRegistryGroup> result = new LinkedHashMap<>();

        for (IModFileInfo modFile : ModList.get().getModFiles()) {
            Path rootPath;
            try {
                rootPath = modFile.getFile().getFilePath();
            } catch (Exception e) {
                continue;
            }
            if (!visitedRoots.add(rootPath)) continue;

            for (IModInfo mod : modFile.getMods()) {
                String modId = mod.getModId();
                try {
                    JsonRegistryGroup root = buildForMod(modId);
                    if (root != null) {
                        result.put(modId, root);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to build JSON tree for mod '{}'", modId, e);
                }
            }
        }

        return result;
    }

    private static void parseFile(Path path, Map<TypeHandler<?>, List<?>> parsed) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) return;

            // Dispatch to top-level handlers
            for (TypeHandler<?> handler : TypeHandlerRegistry.all()) {
                if (root.has(handler.getTypeName())) {
                    for (JsonElement el : root.getAsJsonArray(handler.getTypeName())) {
                        parseAndCollect(parsed, handler, el.getAsJsonObject());
                    }
                }
            }

            // Dispatch embedded types (e.g. block_entity inside block)
            for (TypeHandler<?> handler : TypeHandlerRegistry.all()) {
                if (handler.getParentTypeName() == null) continue;
                TypeHandler<?> parent = TypeHandlerRegistry.get(handler.getParentTypeName());
                if (parent == null || !root.has(parent.getTypeName())) continue;

                for (JsonElement el : root.getAsJsonArray(parent.getTypeName())) {
                    List<JsonObject> embedded = handler.extractEmbedded(el.getAsJsonObject());
                    if (embedded != null) {
                        for (JsonObject emb : embedded) {
                            parseAndCollect(parsed, handler, emb);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing JSON file: {}", path, e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void parseAndCollect(Map parsed, TypeHandler handler, JsonObject json) {
        Object def = handler.parse(json);
        List list = (List) parsed.computeIfAbsent(handler, k -> new ArrayList());
        list.add(def);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void applyUnchecked(TypeHandler handler, Object def, BuildContext context) {
        try {
            handler.apply(def, context);
        } catch (Exception e) {
            LOGGER.error("Failed to apply {} handler: {}", handler.getTypeName(), e.getMessage());
        }
    }

    private static Path findKasugalibDir(String modId) {
        for (IModFileInfo modFile : ModList.get().getModFiles()) {
            for (IModInfo mod : modFile.getMods()) {
                if (mod.getModId().equals(modId)) {
                    IModFile file = modFile.getFile();
                    Path dir = file.findResource("data", modId, "kasugalib");
                    if (Files.isDirectory(dir)) {
                        return dir;
                    }
                }
            }
        }
        return null;
    }

    private JsonTreeBuilder() {}
}
