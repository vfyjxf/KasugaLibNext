package lib.kasuga.scripting.client;

import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.i18n.FMLTranslations;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class ScriptEngineMissingScreen extends ErrorScreen {

    private final Set<String> missingEngines;
    private Screen nextScreen;
    private EngineMissingList entryList;
    private Component errorTitle;

    public ScriptEngineMissingScreen(Set<String> missingEngines, Screen nextScreen) {
        super(Component.literal("Script Engine Missing"), (Component) null);
        this.nextScreen = nextScreen;
        this.missingEngines = missingEngines;
    }

    @Override
    protected void init() {

        int yOffset = 46;

        this.addRenderableWidget(new ExtendedButton(50, this.height - yOffset, this.width - 50 - 50, 20, Component.literal(FMLTranslations.parseMessage("fml.button.open.mods.folder", new Object[0])), (b) -> {
            Util.getPlatform().openFile(FMLPaths.MODSDIR.get().toFile());
        }));

        this.addRenderableWidget(new ExtendedButton(50, this.height - 24, this.width / 2 - 55, 20, Component.literal( "Ignore and Continue(Dangerous!)").withColor(0xffff0000), (b) -> {
            this.minecraft.setScreen(new ConfirmScreen(
                (next)->{
                    if(next) {
                        minecraft.setScreen(this.nextScreen);
                    } else {
                        minecraft.setScreen(this);
                    }
                },
                Component.literal("Continue Without These Script Engines?"),
                Component.literal("You are about to continue without the required Script Engines. This may lead to crashes or unexpected behavior, even breaking your world data. Are you sure you want to proceed?"),
                Component.literal("Yes, I Understand the Risks").withColor(0xffff0000),
                Component.literal("No, Take Me Back")
            ));
        }));

        this.addRenderableWidget(new ExtendedButton(this.width / 2 + 5, this.height - 24, this.width / 2 - 55, 20, Component.translatable("menu.quit"), (b) -> {
            this.minecraft.stop();
        }));

        this.entryList = new EngineMissingList(this.minecraft, width, height - 85, 35, 24, missingEngines);
        this.addWidget(this.entryList);
        this.setFocused(this.entryList);
        this.errorTitle = Component.literal("Some Script Engines are Missing");
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, errorTitle, (this.width - this.font.width(errorTitle)) / 2, 12, 0xffffffff, false);
        this.entryList.render(guiGraphics, mouseX, mouseY, partialTick);
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Getter
    protected static class EngineMissingEntry extends ObjectSelectionList.Entry<EngineMissingEntry> {
        String engineName;

        Button queryButton;

        public EngineMissingEntry(String engineName) {
            this.engineName = engineName;
            queryButton = Button.builder(Component.literal("Download"), (b)->{
                Util.getPlatform().openUri(this.buildModDownloadUrl(engineName));
            }).build();
        }

        private static String SCRIPT_ENGINE_DOWNLOAD_URL = "https://ecosystem.kycraft.cn/search";

        private String buildModDownloadUrl(String engineName) {
            return SCRIPT_ENGINE_DOWNLOAD_URL + "?keyword=" + URLEncoder.encode(engineName, StandardCharsets.UTF_8) + "&platform=" + URLEncoder.encode(Util.getPlatform().telemetryName(), StandardCharsets.UTF_8);
        }

        @Override
        public Component getNarration() {
            return Component.literal(engineName);
        }

        @Override
        public boolean mouseClicked(double p_331676_, double p_330254_, int p_331536_) {
            this.queryButton.mouseClicked(p_331676_, p_330254_, p_331536_);
            return false;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTick) {
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    engineName,
                    left,
                    top + ((entryHeight-Minecraft.getInstance().font.lineHeight) / 2),
                    0xffffffff,
                    false
            );

            queryButton.setPosition(left + entryWidth - 80, top + entryHeight / 2 - 10);

            queryButton.setSize(70, 20);

            queryButton.render(guiGraphics, mouseX, mouseY, partialTick);

        }
    }

    public static class EngineMissingList extends ObjectSelectionList<EngineMissingEntry> {

        public EngineMissingList(Minecraft p_94442_, int p_94443_, int p_94444_, int p_94445_, int p_94446_, Set<String> missingEngines) {
            super(p_94442_, p_94443_, p_94444_, p_94445_, p_94446_);
            for (String missingEngine : missingEngines) {
                this.addEntry(new EngineMissingEntry(missingEngine));
            }

            this.headerHeight = 15;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRight() - 6;
        }

        @Override
        public int getRowWidth() {
            return getWidth() - 32;
        }

        @Override
        protected void enableScissor(GuiGraphics guiGraphics) {
            guiGraphics.enableScissor(this.getX(), this.getY() + this.headerHeight, this.getRight(), this.getBottom());
        }

        @Override
        protected void renderListItems(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderListItems(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal("Script Engine Name"),
                    this.getRowLeft(),
                    this.getY() + 2,
                    0xffffffff,
                    false
            );
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
