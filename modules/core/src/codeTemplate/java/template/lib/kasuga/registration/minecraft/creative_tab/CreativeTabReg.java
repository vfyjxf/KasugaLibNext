package template.lib.kasuga.registration.minecraft.creative_tab;

import lib.kasuga.internal.generator.annotations.CodeTemplate;
import lib.kasuga.internal.generator.annotations.RegGenerator;
import lib.kasuga.internal.generator.facades.RegFacade;
import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import java.util.Collection;

@CodeTemplate(generator = "Reg")
@RegGenerator(modifiers = {
        @RegGenerator.Modifier(
                type = "TabBuilder",
                target = CreativeModeTab.Builder.class,
                enumeration = {
                        "title",
                        "icon",
                        "displayItems",
                        "alignedRight",
                        "hideTitle",
                        "noScrollBar",
                        "backgroundTexture",
                        "withSearchBar",
                        "withScrollBarSpriteLocation",
                        "withTabsImage",
                        "withLabelColor",
                        "withSlotColor",
                        "withTabFactory",
                        "withTabsBefore",
                        "withTabsAfter",
                }
        )
})
public class CreativeTabReg extends MinecraftDeferRegistryReg<CreativeTabReg, CreativeModeTab, CreativeModeTab> {
    public CreativeTabReg(String name) {
        super(name, Registries.CREATIVE_MODE_TAB);
    }

    @Override
    protected CreativeModeTab createObject(ResourceLocation id) {
        return RegFacade.transformObject("TabBuilder", CreativeModeTab.builder()).build();
    }
}
