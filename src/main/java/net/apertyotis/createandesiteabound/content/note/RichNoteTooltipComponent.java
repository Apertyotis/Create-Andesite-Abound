package net.apertyotis.createandesiteabound.content.note;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

public class RichNoteTooltipComponent implements ClientTooltipComponent, TooltipComponent {

    public static final int CLIPBOARD_WIDTH = 244;
    public static final int CLIPBOARD_HEIGHT = 256;
    public static final int LEFT_PADDING = 41;
    public static final int TOP_PADDING = 50;
    public static final int BOTTOM_MARGIN = 18;
    public static final int CONTENT_WIDTH = 162;
    public static final int CONTENT_HEIGHT = 180;

    @Override
    public int getHeight() {
        return CLIPBOARD_HEIGHT + BOTTOM_MARGIN;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return CLIPBOARD_WIDTH;
    }

    public void renderBackground(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(AllGuiTextures.CLIPBOARD.location, x, y, 5, 0, CLIPBOARD_WIDTH, CLIPBOARD_HEIGHT);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        if (!RichNoteClientHandler.active || !RichNoteClientHandler.opened)
            return;
        RichNoteData data = RichNoteDataManager.cachedData;
        if (data == null)
            return;
        data.initPages(font, CONTENT_WIDTH, CONTENT_HEIGHT);
        RichNoteData.Page page = data.getPage();
        if (page == null)
            return;
        renderBackground(guiGraphics, x, y);
        page.render(font, x, y, guiGraphics);
        Component pageIndicator = Component.literal("%d / %d".formatted(data.index + 1, data.pages.size()));
        int width = font.width(pageIndicator);
        int xOffset = (CONTENT_WIDTH - width) / 2;
        guiGraphics.drawString(font, pageIndicator,
            x + LEFT_PADDING + xOffset, y + TOP_PADDING + CONTENT_HEIGHT + 3,
            0x000000, false);
    }
}
