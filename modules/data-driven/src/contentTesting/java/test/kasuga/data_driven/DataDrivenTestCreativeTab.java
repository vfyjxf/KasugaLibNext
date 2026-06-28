package test.kasuga.data_driven;

import io.micronaut.context.annotation.Context;
import lib.kasuga.registration.minecraft.creative_tab.CreativeTabReg;
import lib.kasuga.registration.minecraft.creative_tab.CreativeTabRegModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import test.kasuga.core.CoreTestApplication;

@Context
public class DataDrivenTestCreativeTab {

    public static CreativeTabReg DATA_DRIVEN_TAB = new CreativeTabReg("data_driven_test")
            .configure(CreativeTabRegModifiers.TabBuilder.of("title",
                builder -> builder.title(Component.translatable("itemGroup.kasuga_lib.data_driven_test"))))
            .configure(CreativeTabRegModifiers.TabBuilder.of("icon",
                builder -> builder.icon(() -> new ItemStack(Items.PAPER))))
            .setParent(CoreTestApplication.registry);
}
