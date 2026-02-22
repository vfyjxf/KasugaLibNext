package template.lib.kasuga.registration.minecraft.menu;

import lib.kasuga.internal.generator.annotations.CodeTemplate;
import lib.kasuga.registration.core.RegisterContext;
import lib.kasuga.registration.minecraft.common.MinecraftDeferRegistryReg;
import lib.kasuga.registration.minecraft.menu.MenuRendererBuilder;
import lib.kasuga.registration.stages.RegistrationStage;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;

import java.util.function.Supplier;

@CodeTemplate(generator = "Reg")
public class MenuReg<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> extends MinecraftDeferRegistryReg<MenuReg<T, U>, MenuType<?>, MenuType<T>> {

    private final IContainerFactory<T> supplier;

    private final Supplier<MenuRendererBuilder<T, U>> screenSupplier;

    public MenuReg(String name, IContainerFactory<T> supplier, Supplier<MenuRendererBuilder<T, U>> screenSupplier) {
        super(name, Registries.MENU);
        this.supplier = supplier;
        this.screenSupplier = screenSupplier;
    }

    @Override
    protected MenuType<T> createObject(ResourceLocation id) {
        return new MenuType<>(supplier, FeatureFlagSet.of());
    }

    @Override
    public void register(RegisterContext<?> context) {
        super.register(context);
        if(this.screenSupplier != null){
            context.onStage(RegistrationStage.MENU_SCREEN_BINDING, (ctx)->{
                ctx.register(this.getEntry(), this.screenSupplier);
            });
        }
    }
}
