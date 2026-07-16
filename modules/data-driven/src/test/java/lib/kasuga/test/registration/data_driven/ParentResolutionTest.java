package lib.kasuga.test.registration.data_driven;

import lib.kasuga.registration.Reg;
import lib.kasuga.registration.data_driven.context.JsonRegistryGroup;
import lib.kasuga.registration.data_driven.context.RegBuildContext;
import lib.kasuga.registration.data_driven.handler.RegistryGroupDef;
import lib.kasuga.registration.data_driven.handler.RegistryGroupHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParentResolutionTest {

    @Test
    void childBeforeParentSilentlyAttachesToRoot() {
        var handler = new RegistryGroupHandler();
        var root = new JsonRegistryGroup("test:json_root");
        var context = new RegBuildContext("test", root);

        // define child first (parent not yet stored)
        var childDef = new RegistryGroupDef("test:child", "test:later_parent", null, null);
        handler.apply(childDef, context);

        // define parent second
        var parentDef = new RegistryGroupDef("test:later_parent", null, null, null);
        handler.apply(parentDef, context);

        // child should have parent=later_parent, but current code will have parent=root
        // because later_parent wasn't stored yet when child was applied.
        JsonRegistryGroup child = context.getRegistryGroup("test:child");
        assertNotNull(child);

        // parent resolution happens via Reg.setParent/getChildren — walk root to find child
        Reg<?, ?> childInTree = findChildById(root, "test:child");
        assertNotNull(childInTree, "child should be in registry tree");

        Reg<?, ?> childParent = findParentOf(root, childInTree);
        assertNotNull(childParent, "child should have a parent");

        boolean parentIsRoot = childParent == root;
        // Current bug: parentIsRoot == true because parent wasn't resolved yet
        assertTrue(parentIsRoot,
            "BUG: child before parent silently attached to root. parent=" +
            (childParent instanceof JsonRegistryGroup g ? g.getGroupId() : childParent.getClass().getSimpleName()));
    }

    @Test
    void parentBeforeChildResolvesCorrectly() {
        var handler = new RegistryGroupHandler();
        var root = new JsonRegistryGroup("test:json_root");
        var context = new RegBuildContext("test", root);

        // define parent first
        handler.apply(new RegistryGroupDef("test:parent_first", null, null, null), context);
        // define child second, with parent already in context
        handler.apply(new RegistryGroupDef("test:child_second", "test:parent_first", null, null), context);

        Reg<?, ?> childInTree = findChildById(root, "test:child_second");
        assertNotNull(childInTree);

        Reg<?, ?> childParent = findParentOf(root, childInTree);
        assertNotNull(childParent);
        assertTrue(childParent instanceof JsonRegistryGroup);
        assertEquals("test:parent_first", ((JsonRegistryGroup) childParent).getGroupId(),
            "parent defined first should be resolved correctly");
    }

    /** Search the tree for a child whose JsonRegistryGroup.groupId matches the suffix. */
    private static Reg<?, ?> findChildById(Reg<?, ?> node, String id) {
        for (Reg<?, ?> child : node.getChildren()) {
            if (child instanceof JsonRegistryGroup g && g.getGroupId().equals(id)) return child;
            Reg<?, ?> found = findChildById(child, id);
            if (found != null) return found;
        }
        return null;
    }

    /** Find the parent of target within the tree rooted at root. */
    private static Reg<?, ?> findParentOf(Reg<?, ?> root, Reg<?, ?> target) {
        for (Reg<?, ?> child : root.getChildren()) {
            if (child == target) return root;
            Reg<?, ?> found = findParentOf(child, target);
            if (found != null) return found;
        }
        return null;
    }
}
