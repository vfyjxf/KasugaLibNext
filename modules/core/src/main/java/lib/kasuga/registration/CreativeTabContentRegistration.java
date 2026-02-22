package lib.kasuga.registration;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.function.BiConsumer;

public class CreativeTabContentRegistration {

    private final ResourceKey<CreativeModeTab> tabKey;
    private final CreativeModeTab tab;
    private final BuildCreativeModeTabContentsEvent event;

    public CreativeTabContentRegistration(ResourceKey<CreativeModeTab> tabKey, CreativeModeTab tab, BuildCreativeModeTabContentsEvent event) {
        this.tabKey = tabKey;
        this.tab = tab;
        this.event = event;
    }

    public CreativeModeTab getTab() {
        return tab;
    }

    public ResourceKey<CreativeModeTab> getTabKey() {
        return tabKey;
    }

    public BuildCreativeModeTabContentsEvent getEvent() {
        return event;
    }
}
