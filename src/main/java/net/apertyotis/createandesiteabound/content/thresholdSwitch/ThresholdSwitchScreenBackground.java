package net.apertyotis.createandesiteabound.content.thresholdSwitch;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("removal")
public class ThresholdSwitchScreenBackground {
    public static final ResourceLocation curiosities = new ResourceLocation(Create.ID, "textures/gui/curiosities.png");
    public static final ResourceLocation logistics = new ResourceLocation(Create.ID, "textures/gui/logistics.png");
    public static void render(GuiGraphics graphics, int x, int y, boolean forItemsOrFluid) {
        graphics.blit(logistics, x, y, 0, 0,174, 15);
        graphics.blit(curiosities, x + 1, y + 15, 1, 146, 20, 49);
        graphics.blit(curiosities, x + 21, y + 15, 21, 146, 150, 6);
        graphics.blit(curiosities, x + 171, y + 15, 177, 146, 2, 49);
        graphics.blit(curiosities, x + 21, y + 21, 21, 154, 150, 18);
        graphics.blit(curiosities, x + 21, y + 39, 21, 152, 150, 20);
        graphics.blit(curiosities, x + 21, y + 59, 21, 194, 150, 5);
        graphics.blit(curiosities, x + 1, y + 64, 1, 201, 170, 2);
        graphics.blit(curiosities, x + 171, y + 64, 177, 201, 2, 2);
        if (forItemsOrFluid) {
            graphics.blit(curiosities, x + 93, y + 21, 73, 40, 4, 18);
            graphics.blit(curiosities, x + 93, y + 41, 73, 40, 4, 18);
        }
        graphics.blit(logistics, x, y + 65, 0, 65, 182, 30);
        AllGuiTextures.BUTTON_DOWN.render(graphics, x + 12, y + 71);
    }
}
