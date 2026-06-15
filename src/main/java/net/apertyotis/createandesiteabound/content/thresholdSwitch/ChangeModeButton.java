package net.apertyotis.createandesiteabound.content.thresholdSwitch;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.ScreenElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;

public class ChangeModeButton extends IconButton {
    public boolean down = false;

    public ChangeModeButton(int x, int y, ScreenElement icon) {
        super(x, y, icon);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        down = !down;
        super.onClick(mouseX, mouseY);
    }

    @Override
    public void doRender(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;

            AllGuiTextures button = (!active || down) ? AllGuiTextures.BUTTON_DOWN :
                isMouseOver(mouseX, mouseY) ? AllGuiTextures.BUTTON_HOVER : AllGuiTextures.BUTTON;

            if (down && isMouseOver(mouseX, mouseY)) {
                RenderSystem.setShaderColor(1.2F, 1.2F, 1.2F, 1.0F);
                drawBg(graphics, button);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                drawBg(graphics, button);
            }
            icon.render(graphics, getX() + 1, getY() + 1);
        }
    }
}
