package lib.kasuga.registration.data_driven;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import lib.kasuga.content.graph.GraphCycleDetector;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.RegistryGroup;
import lib.kasuga.registration.core.Modifier;
import lib.kasuga.registration.core.ResourceLocationModifiers;
import lib.kasuga.registration.factory.FactoryRegistry;
import lib.kasuga.registration.minecraft.item.ItemRegModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class JsonTreeBuilder {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    private record RawGroup(String id, String parent, JsonObject properties, JsonObject itemProperties) {}
    private record RawBlock(String id, String type, String group, JsonObject properties,
                            JsonObject itemProperties, JsonObject modelData, String blockEntityType) {}

    private record RawItem(String id, String type, String group, JsonObject properties) {}

    private record RawData(Map<String, RawGroup> groups, List<RawBlock> blocks, List<RawItem> items) {}

    /**
     * Scans a specific mod's JSON files and builds the virtual registration tree.
     * Returns the root JsonRegistryGroup, or {@code null} if no JSON files were found.
     * <p>
     * Attach the returned group to a net.neoforged.neoforge.registries.Registry
     * via {@code setParent()} or {@code addChild()} to integrate with the existing tree.
     */
    public static JsonRegistryGroup buildForMod(String modId) {
        LOGGER.info("[buildForMod] called for modId='{}'", modId);
        Path kasugalibDir = findKasugalibDir(modId);
        if (kasugalibDir == null) return null;

        RawData data = scanFolder(kasugalibDir);
        if (data.groups().isEmpty() && data.blocks().isEmpty() && data.items().isEmpty()) return null;

        LOGGER.info("Building JSON registration tree for mod '{}' with {} groups, {} blocks, {} items",
                modId, data.groups().size(), data.blocks().size(), data.items().size());
        return buildTree(modId, data);
    }

    /**
     * Scans all mods and returns their virtual trees.
     */
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

    private static RawData scanFolder(Path dir) {
        Map<String, RawGroup> groups = new LinkedHashMap<>();
        List<RawBlock> blocks = new ArrayList<>();
        List<RawItem> items = new ArrayList<>();

        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.toString().endsWith(".json"))
                 .sorted()
                 .forEach(path -> parseFile(path, groups, blocks, items));
        } catch (IOException e) {
            LOGGER.error("Error scanning directory: {}", dir, e);
        }

        return new RawData(groups, blocks, items);
    }

    private static void parseFile(Path path, Map<String, RawGroup> groups,
                                   List<RawBlock> blocks, List<RawItem> items) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) return;

            if (root.has("groups")) {
                for (JsonElement el : root.getAsJsonArray("groups")) {
                    JsonObject g = el.getAsJsonObject();
                    String id = g.get("id").getAsString();
                    if (groups.containsKey(id)) {
                        LOGGER.warn("Duplicate group id '{}' in {}, skipping", id, path);
                        continue;
                    }
                    String parent = g.has("parent") ? g.get("parent").getAsString() : null;
                    JsonObject props = g.has("properties") ? g.getAsJsonObject("properties") : null;
                    JsonObject itemProps = g.has("item_properties") ? g.getAsJsonObject("item_properties") : null;
                    groups.put(id, new RawGroup(id, parent, props, itemProps));
                }
            }

            if (root.has("blocks")) {
                for (JsonElement el : root.getAsJsonArray("blocks")) {
                    JsonObject b = el.getAsJsonObject();
                    String id = b.get("id").getAsString();
                    String type = b.get("type").getAsString();
                    String group = b.has("group") ? b.get("group").getAsString() : null;
                    JsonObject props = b.has("properties") ? b.getAsJsonObject("properties") : null;
                    JsonObject itemProps = b.has("item_properties") ? b.getAsJsonObject("item_properties") : null;
                    JsonObject modelData = new JsonObject();
                    if (b.has("model")) modelData.addProperty("model", b.get("model").getAsString());
                    if (b.has("textures")) modelData.add("textures", b.get("textures"));
                    if (b.has("state_machine")) modelData.addProperty("state_machine", b.get("state_machine").getAsString());

                    // Parse block_entity reference
                    String blockEntityType = null;
                    if (b.has("block_entity")) {
                        JsonObject beObj = b.getAsJsonObject("block_entity");
                        if (beObj.has("type")) {
                            blockEntityType = beObj.get("type").getAsString();
                        }
                        modelData.add("block_entity", b.get("block_entity"));
                        LOGGER.info("[parseFile] block '{}' has block_entity, parsed type='{}'", id, blockEntityType);
                    }

                    blocks.add(new RawBlock(id, type, group, props, itemProps, modelData, blockEntityType));
                }
            }

            if (root.has("items")) {
                for (JsonElement el : root.getAsJsonArray("items")) {
                    JsonObject i = el.getAsJsonObject();
                    String id = i.get("id").getAsString();
                    String type = i.get("type").getAsString();
                    String group = i.has("group") ? i.get("group").getAsString() : null;
                    JsonObject props = i.has("properties") ? i.getAsJsonObject("properties") : null;
                    items.add(new RawItem(id, type, group, props));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing JSON file: {}", path, e);
        }
    }

    private static JsonRegistryGroup buildTree(String modId, RawData data) {
        JsonRegistryGroup rootGroup = new JsonRegistryGroup(modId + ":json_root");
        rootGroup.configure(ResourceLocationModifiers.withNamespace(modId));

        Map<String, RawGroup> groupDefs = data.groups();

        // Topological sort groups to ensure parents are created before children
        List<String> orderedIds;
        if (!groupDefs.isEmpty()) {
            GraphCycleDetector.TopoResult<String> topo = GraphCycleDetector.topologicalSort(
                groupDefs.keySet(),
                id -> {
                    RawGroup g = groupDefs.get(id);
                    return g.parent() != null && groupDefs.containsKey(g.parent())
                        ? List.of(g.parent()) : List.of();
                });

            if (topo.hasCycle()) {
                LOGGER.error("Group cycle detected: nodes not sorted = {}", topo.getNodesNotSorted());
                GraphCycleDetector.CycleResult<String> cycles = GraphCycleDetector.dfs(
                    topo.getNodesNotSorted(),
                    id -> {
                        RawGroup g = groupDefs.get(id);
                        return g.parent() != null && groupDefs.containsKey(g.parent())
                            ? List.of(g.parent()) : List.of();
                    });
                for (List<String> cycle : cycles.getCycles()) {
                    LOGGER.error("  Cycle: {} → {}", String.join(" → ", cycle), cycle.get(0));
                }
            }
            orderedIds = topo.getSorted();
        } else {
            orderedIds = List.of();
        }

        // Create group instances
        Map<String, JsonRegistryGroup> groupMap = new LinkedHashMap<>();
        for (String id : orderedIds) {
            RawGroup def = groupDefs.get(id);
            JsonRegistryGroup group = new JsonRegistryGroup(id);
            groupMap.put(id, group);

            // Attach to parent or root
            if (def.parent() != null && groupMap.containsKey(def.parent())) {
                group.setParent(groupMap.get(def.parent()));
            } else if (def.parent() != null) {
                LOGGER.warn("Group '{}' references unknown parent '{}', attaching to root", id, def.parent());
                group.setParent(rootGroup);
            } else {
                group.setParent(rootGroup);
            }

            // Inject group-level properties as modifiers
            if (def.properties() != null) {
                List<Modifier<BlockBehaviour.Properties>> mods = JsonPropertyParser.getInstance().parseBlockProperties(def.properties());
                for (Modifier<BlockBehaviour.Properties> m : mods) {
                    group.configure(m);
                }
            }
        }

        // Track created block regs for BE association
        Map<String, Reg<?, Block>> blockRegMap = new LinkedHashMap<>();

        // Create block regs and attach to groups
        for (RawBlock def : data.blocks()) {
            try {
                Reg<?, Block> blockReg = createBlock(def, groupDefs, groupMap, rootGroup);
                if (blockReg != null) {
                    blockRegMap.put(def.id(), blockReg);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to create block '{}': {}", def.id(), e.getMessage());
            }
        }

        // Create block entities for blocks that reference them
        for (RawBlock def : data.blocks()) {
            LOGGER.info("[buildTree] block '{}' blockEntityType={}", def.id(), def.blockEntityType());
            if (def.blockEntityType() != null) {
                try {
                    createBlockEntity(def, blockRegMap);
                } catch (Exception e) {
                    LOGGER.error("Failed to create block entity for block '{}': {}", def.id(), e.getMessage());
                }
            }
        }

        // Create standalone items
        for (RawItem def : data.items()) {
            try {
                createItem(def, groupDefs, groupMap, rootGroup);
            } catch (Exception e) {
                LOGGER.error("Failed to create item '{}': {}", def.id(), e.getMessage());
            }
        }

        return rootGroup;
    }

    private static Reg<?, Block> createBlock(RawBlock def, Map<String, RawGroup> groupDefs,
                                              Map<String, JsonRegistryGroup> groupMap,
                                              RegistryGroup rootGroup) {
        LOGGER.info("[createBlock] creating block '{}' type='{}' group='{}'", def.id(), def.type(), def.group());
        if (!FactoryRegistry.contains(def.type())) {
            LOGGER.warn("Unknown block type '{}' for block '{}'", def.type(), def.id());
            return null;
        }

        String[] parts = def.id().split(":", 2);
        String path = parts.length > 1 ? parts[1] : parts[0];
        String namespace = parts.length > 1 ? parts[0] : "minecraft";

        FactoryRegistry.BlockFactory factory = FactoryRegistry.get(def.type());
        Reg<?, Block> reg = factory.create(path);

        // Override namespace if different from root
        if (!namespace.equals("minecraft")) {
            reg.configure(ResourceLocationModifiers.withNamespace(namespace));
        }

        // Inject block-level properties as modifiers
        if (def.properties() != null) {
            List<Modifier<BlockBehaviour.Properties>> mods = JsonPropertyParser.getInstance().parseBlockProperties(def.properties());
            for (Modifier<BlockBehaviour.Properties> m : mods) {
                reg.configure(m);
            }
        }

        // Register creative tab: per-block item_properties.tab, or inherit from group
        applyCreativeTab(reg, def.itemProperties(), def.group(), groupDefs);

        // Attach to group
        if (def.group() != null && groupMap.containsKey(def.group())) {
            reg.setParent(groupMap.get(def.group()));
        } else if (def.group() != null) {
            LOGGER.warn("Block '{}' references unknown group '{}', attaching to root", def.id(), def.group());
            reg.setParent(rootGroup);
        } else {
            reg.setParent(rootGroup);
        }

        return reg;
    }

    private static void createItem(RawItem def, Map<String, RawGroup> groupDefs,
                                    Map<String, JsonRegistryGroup> groupMap,
                                    RegistryGroup rootGroup) {
        if (!FactoryRegistry.containsItem(def.type())) {
            LOGGER.warn("Unknown item type '{}' for item '{}'", def.type(), def.id());
            return;
        }

        String[] parts = def.id().split(":", 2);
        String path = parts.length > 1 ? parts[1] : parts[0];
        String namespace = parts.length > 1 ? parts[0] : "minecraft";

        FactoryRegistry.ItemFactory factory = FactoryRegistry.getItemFactory(def.type());
        Reg<?, Item> reg = factory.create(path);

        // Override namespace if different from root
        if (!namespace.equals("minecraft")) {
            reg.configure(ResourceLocationModifiers.withNamespace(namespace));
        }

        // Inject item-level properties as modifiers
        if (def.properties() != null) {
            List<Modifier<Item.Properties>> mods = JsonItemParser.INSTANCE.parseItemProperties(def.properties());
            for (Modifier<Item.Properties> m : mods) {
                reg.configure(m);
            }
        }

        // Register creative tab: per-item properties.tab, or inherit from group
        applyCreativeTab(reg, def.properties(), def.group(), groupDefs);

        // Attach to group
        if (def.group() != null && groupMap.containsKey(def.group())) {
            reg.setParent(groupMap.get(def.group()));
        } else if (def.group() != null) {
            LOGGER.warn("Item '{}' references unknown group '{}', attaching to root", def.id(), def.group());
            reg.setParent(rootGroup);
        } else {
            reg.setParent(rootGroup);
        }
    }

    private static void createBlockEntity(RawBlock blockDef, Map<String, Reg<?, Block>> blockRegMap) {
        String beType = blockDef.blockEntityType();
        LOGGER.info("[createBlockEntity] block='{}' beType='{}'", blockDef.id(), beType);
        if (!FactoryRegistry.containsBlockEntity(beType)) {
            LOGGER.warn("Unknown block entity type '{}' for block '{}', registered types: {}",
                    beType, blockDef.id(), FactoryRegistry.getBlockEntityTypes());
            return;
        }

        // Get the block reg for valid blocks
        Reg<?, Block> blockReg = blockRegMap.get(blockDef.id());
        if (blockReg == null) {
            LOGGER.warn("Block '{}' not found for block entity association, available blocks: {}",
                    blockDef.id(), blockRegMap.keySet());
            return;
        }

        String beName = blockDef.id().split(":", 2).length > 1
                ? blockDef.id().split(":", 2)[1] + "_be"
                : blockDef.id() + "_be";

        LOGGER.info("[createBlockEntity] creating BE '{}' with factory '{}'", beName, beType);
        FactoryRegistry.BlockEntityFactory factory = FactoryRegistry.getBlockEntityFactory(beType);
        Reg<?, ?> beReg = factory.create(beName, () -> new Block[]{blockReg.getEntry()});

        // Attach BE as child of the block so it registers alongside it
        blockReg.addChild(beReg);
        LOGGER.info("[createBlockEntity] BE '{}' attached to block '{}'", beName, blockDef.id());
    }

    /**
     * Applies creative tab registration from item_properties.tab.
     * Checks per-entry first, then falls back to group inheritance.
     */
    private static void applyCreativeTab(Reg<?, ?> reg, JsonObject entryItemProps,
                                          String groupId, Map<String, RawGroup> groupDefs) {
        JsonObject effectiveItemProps = entryItemProps;
        if ((effectiveItemProps == null || !effectiveItemProps.has("tab"))
                && groupId != null && groupDefs.containsKey(groupId)) {
            RawGroup group = groupDefs.get(groupId);
            if (group.itemProperties() != null && group.itemProperties().has("tab")) {
                effectiveItemProps = group.itemProperties();
            }
        }
        if (effectiveItemProps != null && effectiveItemProps.has("tab")) {
            String tabStr = effectiveItemProps.get("tab").getAsString();
            ResourceLocation tabId = ResourceLocation.parse(tabStr);
            reg.configure(ItemRegModifiers.TAB_TO_BY_KEY_BY_SUPPLIER.apply(() -> tabId));
        }
    }

    private JsonTreeBuilder() {}
}
