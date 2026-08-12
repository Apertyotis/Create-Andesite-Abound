package net.apertyotis.createandesiteabound.foundation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.utility.Couple;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.common.util.MathUtil;
import mezz.jei.library.gui.ingredients.TagContentTooltipComponent;
import net.apertyotis.createandesiteabound.mixin.jei.RecipeSlotAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AssemblyContentTooltipComponent<T> extends TagContentTooltipComponent<T> {

    public final AssemblyJunkSlotWidget widget;
    public int lineCount;
    public int drawCount;
    public int maxPerLine;

    public AssemblyContentTooltipComponent(
        IIngredientRenderer<T> renderer, List<T> ingredients, AssemblyJunkSlotWidget widget
    ) {
        super(renderer, ingredients);
        this.widget = widget;

        lineCount = MathUtil.divideCeil(ingredients.size(), 10);
        lineCount = Math.min(lineCount, 3);
        drawCount = ingredients.size() <= 30 ? ingredients.size() : 29;
        maxPerLine = MathUtil.divideCeil(drawCount, lineCount);
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics guiGraphics) {
        for (int i = 1; i < widget.recipe.resultPool.size(); i++) {
            ProcessingOutput output = widget.recipe.resultPool.get(i);
            Couple<Long> frac = AssemblyJunkSlotWidget.fraction(output.getChance(), widget.totalWeight);

            int column = (i - 1) % maxPerLine;
            int row = (i - 1) / maxPerLine;
            int pX = x + column * 19;
            int pY = y + row * 19;
            guiGraphics.blit(InventoryScreen.INVENTORY_LOCATION, pX, pY, 7, 83, 18, 18);
            guiGraphics.renderFakeItem(output.getStack(), pX + 1, pY + 1);
            guiGraphics.renderItemDecorations(font, output.getStack(), pX + 1, pY + 1);

            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            String chance = "%d/%d".formatted(frac.getFirst(), frac.getSecond());
            int width = font.width(chance);
            float scale = width < 16 ? 1 : 16f / width;
            float xOffset = width < 16 ? (16 - width) / 2f : 0;
            float yOffset = 16 - font.lineHeight * scale;
            poseStack.translate(pX + xOffset + 1, pY + yOffset + 1, 200);
            poseStack.scale(scale, scale, scale);
            guiGraphics.drawString(font, chance, 0, 0, 0xFFFFFF, true);
            poseStack.popPose();
        }

        int current = 0;
        List<?> visible = ((RecipeSlotAccessor) widget.slot).getDisplayIngredients();
        if (visible != null && visible.size() != widget.recipe.resultPool.size() - 1) {
            ItemStack stack = widget.slot.getDisplayedItemStack().orElse(ItemStack.EMPTY);
            for (int i = 1; i < widget.recipe.resultPool.size(); i++) {
                if (stack.equals(widget.recipe.resultPool.get(i).getStack(), true))
                    break;
                current++;
            }
        } else {
            current = widget.index;
        }

        int column = current % maxPerLine;
        int row = current / maxPerLine;
        int pX = x + column * 19 + 1;
        int pY = y + row * 19 + 1;
        guiGraphics.fill(pX, pY, pX + 16, pY + 16, 0x80FFFFFF);
    }
}
