package net.apertyotis.createandesiteabound.content.schematic.wrapper;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

public class ChecklistTooltipComponent implements ClientTooltipComponent, TooltipComponent {

    public static final int CLIPBOARD_WIDTH = 228;
    public static final int CLIPBOARD_HEIGHT = 256;

    @Override
    public int getHeight() {
        return CLIPBOARD_HEIGHT;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return CLIPBOARD_WIDTH;
    }

    public void renderBackground(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(AllGuiTextures.CLIPBOARD.location, x, y, 13, 0, CLIPBOARD_WIDTH, CLIPBOARD_HEIGHT);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        if (!SimpleWrapperTooltipHandler.active || !SimpleWrapperTooltipHandler.opened)
            return;
        renderBackground(guiGraphics, x, y);
        ChecklistPageManager.render(font, x, y, guiGraphics);
    }
}
