package lib.kasuga.registration.stages;

import lib.kasuga.registration.minecraft.menu.MenuRendererBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import java.util.function.Supplier;

public interface MenuScreenBindingStage {
    public <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void register(MenuType<T> menuType, Supplier<MenuRendererBuilder<T, U>> builder);

    @OnlyIn(Dist.CLIENT)
    public static class Instance implements MenuScreenBindingStage {
        protected RegisterMenuScreensEvent event;

        public Instance(RegisterMenuScreensEvent event) {
            this.event = event;
        }

        public <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void register(MenuType<T> menuType, Supplier<MenuRendererBuilder<T, U>> builderSupplier) {
            event.register((MenuType<T>)menuType, builderSupplier.get()::create);
        }
    }
}
