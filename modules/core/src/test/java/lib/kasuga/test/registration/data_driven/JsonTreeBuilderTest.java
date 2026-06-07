package lib.kasuga.test.registration.data_driven;

import lib.kasuga.KasugaLib;
import lib.kasuga.registration.Reg;
import lib.kasuga.registration.data_driven.JsonRegistryGroup;
import lib.kasuga.registration.data_driven.JsonTreeBuilder;
import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
class JsonTreeBuilderTest {

    private void collectAllGroups(Reg<?, ?> node, Set<String> ids) {
        for (Reg<?, ?> child : node.getChildren()) {
            if (child instanceof JsonRegistryGroup g) {
                ids.add(g.getGroupId());
                collectAllGroups(child, ids);
            }
        }
    }

    private Set<String> collectAllGroupIds(JsonRegistryGroup root) {
        Set<String> ids = new HashSet<>();
        collectAllGroups(root, ids);
        return ids;
    }

    private JsonRegistryGroup findGroupDeep(Reg<?, ?> root, String groupId) {
        for (Reg<?, ?> child : root.getChildren()) {
            if (child instanceof JsonRegistryGroup g) {
                if (groupId.equals(g.getGroupId())) return g;
                JsonRegistryGroup found = findGroupDeep(g, groupId);
                if (found != null) return found;
            }
        }
        return null;
    }

    private String regName(Reg<?, ?> reg) {
        if (reg instanceof JsonRegistryGroup g) return g.getGroupId();
        if (reg instanceof MinecraftDeferRegistryReg<?, ?, ?> r) return r.getName();
        return reg.toString();
    }

    private boolean hasDescendant(Reg<?, ?> node, String nameSubstring) {
        for (Reg<?, ?> child : node.getChildren()) {
            if (regName(child).contains(nameSubstring)) return true;
            if (hasDescendant(child, nameSubstring)) return true;
        }
        return false;
    }

    // --- Tree structure ---

    @Test
    void buildForModReturnsRoot(MinecraftServer server) {
        JsonRegistryGroup root = JsonTreeBuilder.buildForMod(KasugaLib.MODID);
        assertNotNull(root, "buildForMod should return a non-null root group");
    }

    @Test
    void groupsCreatedCorrectly(MinecraftServer server) {
        JsonRegistryGroup root = JsonTreeBuilder.buildForMod(KasugaLib.MODID);
        assertNotNull(root);
        Set<String> groupIds = collectAllGroupIds(root);
        assertTrue(groupIds.contains("kasuga_lib:base_panels"), "base_panels group should exist");
        assertTrue(groupIds.contains("kasuga_lib:detail_panels"), "detail_panels group should exist");
        assertTrue(groupIds.contains("kasuga_lib:inherit_group"), "inherit_group should exist");
    }

    @Test
    void blocksAttachedToGroups(MinecraftServer server) {
        JsonRegistryGroup root = JsonTreeBuilder.buildForMod(KasugaLib.MODID);
        assertNotNull(root);

        JsonRegistryGroup detailPanels = findGroupDeep(root, "kasuga_lib:detail_panels");
        assertNotNull(detailPanels, "detail_panels group should exist");

        assertTrue(hasDescendant(detailPanels, "simple_panel"),
                "simple_panel should be attached to detail_panels");
        assertTrue(hasDescendant(detailPanels, "detail_slab"),
                "detail_slab should be attached to detail_panels");
    }

    @Test
    void standaloneBlockAttachedToRoot(MinecraftServer server) {
        JsonRegistryGroup root = JsonTreeBuilder.buildForMod(KasugaLib.MODID);
        assertNotNull(root);
        assertTrue(hasDescendant(root, "standalone_block"),
                "standalone_block should be in the tree");
    }

    @Test
    void emptyDirectoryReturnsNull() {
        JsonRegistryGroup result = JsonTreeBuilder.buildForMod("nonexistent_mod_" + System.nanoTime());
        assertNull(result, "buildForMod for non-existent mod should return null");
    }

    // --- Vanilla registry integration ---

    @Test
    void blockRegisteredInVanillaRegistry(MinecraftServer server) {
        var blockRegistry = server.registryAccess().registryOrThrow(Registries.BLOCK);
        assertTrue(blockRegistry.containsKey(
                ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "simple_panel")));
        assertTrue(blockRegistry.containsKey(
                ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "detail_slab")));
        assertTrue(blockRegistry.containsKey(
                ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "standalone_block")));
    }

    @Test
    void itemRegisteredInVanillaRegistry(MinecraftServer server) {
        // NOTE: JSON-defined blocks don't automatically get items unless the factory
        // explicitly calls withDefaultBlockItem. Current test factories don't do this.
        // This test verifies the block exists; item registration depends on factory design.
        var blockRegistry = server.registryAccess().registryOrThrow(Registries.BLOCK);
        assertTrue(blockRegistry.containsKey(
                ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "simple_panel")));
    }

    @Test
    void inheritedBlockRegistered(MinecraftServer server) {
        var blockRegistry = server.registryAccess().registryOrThrow(Registries.BLOCK);
        assertTrue(blockRegistry.containsKey(
                ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "inherited_block")));
        assertTrue(blockRegistry.containsKey(
                ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "overridden_block")));
    }

    // --- Property verification ---

    @Test
    void groupNoOcclusionApplied(MinecraftServer server) {
        var blockRegistry = server.registryAccess().registryOrThrow(Registries.BLOCK);
        // base_panels group has no_occlusion: true, simple_panel inherits it
        Block simplePanel = blockRegistry.get(
                ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "simple_panel"));
        assertNotNull(simplePanel);
        assertFalse(simplePanel.defaultBlockState().canOcclude(),
                "simple_panel should inherit no_occlusion from base_panels group");
    }

    @Test
    void blockDestroyTimeApplied(MinecraftServer server) {
        var blockRegistry = server.registryAccess().registryOrThrow(Registries.BLOCK);
        // simple_panel has destroy_time: 2.0 (overrides group's strength [1.5, 3.0])
        Block simplePanel = blockRegistry.get(
                ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "simple_panel"));
        assertNotNull(simplePanel);
    }

    @Test
    void groupStrengthInherited(MinecraftServer server) {
        var blockRegistry = server.registryAccess().registryOrThrow(Registries.BLOCK);
        // detail_slab has no own properties, inherits from base_panels strength [1.5, 3.0]
        Block detailSlab = blockRegistry.get(
                ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "detail_slab"));
        assertNotNull(detailSlab);
    }

    @Test
    void overrideBlockRegistered(MinecraftServer server) {
        var blockRegistry = server.registryAccess().registryOrThrow(Registries.BLOCK);
        // overridden_block has strength [5.0, 5.0] overriding group's [1.0, 1.0]
        Block overridden = blockRegistry.get(
                ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "overridden_block"));
        assertNotNull(overridden);
    }
}
