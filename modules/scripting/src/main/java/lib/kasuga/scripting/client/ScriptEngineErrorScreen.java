package lib.kasuga.scripting.client;

import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.fml.i18n.FMLTranslations;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.apache.commons.io.output.StringBuilderWriter;
import org.codehaus.plexus.util.StringOutputStream;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ScriptEngineErrorScreen extends ErrorScreen {

    private final Map<String, List<Throwable>> errors;
    private Screen nextScreen;
    private EngineMissingList entryList;
    private Component errorTitle;

    public ScriptEngineErrorScreen(Map<String, List<Throwable>> errors, Screen nextScreen) {
        super(Component.literal("Failed to Construct Script Engine"), (Component) null);
        this.nextScreen = nextScreen;
        this.errors = errors;
    }

    @Override
    protected void init() {
        int yOffset = 46;

        this.addRenderableWidget(new ExtendedButton(50, this.height - yOffset, this.width / 2 - 55, 20, Component.literal(FMLTranslations.parseMessage("fml.button.open.mods.folder", new Object[0])), (b) -> {
            Util.getPlatform().openFile(FMLPaths.MODSDIR.get().toFile());
        }));


        this.addRenderableWidget(new ExtendedButton(this.width / 2 + 5, this.height - yOffset, this.width / 2 - 55, 20, Component.literal(FMLTranslations.parseMessage("fml.button.open.log", new Object[0])), (b) -> {
            Util.getPlatform().openFile(FMLPaths.GAMEDIR.get().resolve(Paths.get("logs", "latest.log")).toFile());
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

        this.entryList = new EngineMissingList(this.minecraft, width, height - 85, 35, 24, errors);
        this.addWidget(this.entryList);
        this.setFocused(this.entryList);
        this.errorTitle = Component.literal("Failed to Construct Script Engines With No Alternatives Found");
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, errorTitle, this.width / 2, 12, 0xffffffff);
        this.entryList.render(guiGraphics, mouseX, mouseY, partialTick);
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Getter
    protected static class ScriptEngineErrorEntry extends ObjectSelectionList.Entry<ScriptEngineErrorEntry> {
        public final Throwable throwable;
        public List<FormattedCharSequence> stackTrace;
        String engineName;
        FormattedText message;

        public ScriptEngineErrorEntry(String engineName, FormattedText message, Throwable throwable, List<FormattedCharSequence> stackTrace) {
            this.engineName = engineName;
            this.message = message;
            this.throwable = throwable;
            this.stackTrace = stackTrace;
        }

        @Override
        public Component getNarration() {
            return Component.literal(engineName);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTick) {
            Font font = Minecraft.getInstance().font;
            List<FormattedCharSequence> strings = font.split(this.message, entryWidth - 110 - 10);
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    engineName,
                    left,
                    top + ((entryHeight-Minecraft.getInstance().font.lineHeight) / 2),
                    0xffffffff,
                    false
            );

            int y = top + 2;
            for (FormattedCharSequence string : strings) {
                guiGraphics.drawString(font, string, left + 110 + 5, y, 0xFFFFFFFF, false);
                y += font.lineHeight;
            }
        }
    }

    public static class EngineMissingList extends ObjectSelectionList<ScriptEngineErrorEntry> {

        public EngineMissingList(Minecraft p_94442_, int p_94443_, int p_94444_, int p_94445_, int p_94446_, Map<String, List<Throwable>> errors) {
            super(p_94442_, p_94443_, p_94444_, p_94445_, errors.values().stream().flatMap(Collection::stream).mapToInt((error) -> {
                return Minecraft.getInstance().font.split(Component.literal(error.toString()), p_94443_  - 110 - 10 - 32 - 15).size();
            }).max().orElse(0) * 9 + 8);



            for (Map.Entry<String, List<Throwable>> entry : errors.entrySet()) {
                String engineName = entry.getKey();
                List<Throwable> throwableList = entry.getValue();

                for (Throwable throwable : throwableList) {
                    StringBuilder sb = new StringBuilder();

                    try(StringBuilderWriter writer = new StringBuilderWriter(sb)) {
                        throwable.printStackTrace(new PrintWriter(writer));
                    }

                    this.addEntry(new ScriptEngineErrorEntry(engineName, Component.literal(throwable.toString()), throwable,
                            Minecraft.getInstance().font.split(Component.literal(
                                    sb.toString()
                            ), width - 120)));
                }
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
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal("Script Engine Name"),
                    this.getRowLeft(),
                    this.getY() + 2,
                    0xffffffff,
                    false
            );

            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal("Exception"),
                    this.getRowLeft() + 115,
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
