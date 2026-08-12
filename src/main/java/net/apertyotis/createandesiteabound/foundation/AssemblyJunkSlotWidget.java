package net.apertyotis.createandesiteabound.foundation;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.utility.Couple;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.common.util.ImmutableRect2i;
import net.apertyotis.createandesiteabound.mixin.jei.RecipeSlotAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class AssemblyJunkSlotWidget implements IRecipeWidget, IJeiInputHandler, ISlottedRecipeWidget {

    public final SequencedAssemblyRecipe recipe;
    public final IRecipeSlotDrawable slot;
    public final ImmutableRect2i rect;
    public int ticker = 0;
    public int index = 0;
    public float totalWeight = 0;

    @SuppressWarnings("removal")
    public AssemblyJunkSlotWidget(SequencedAssemblyRecipe recipe, IRecipeSlotDrawable slot) {
        this.recipe = recipe;
        this.slot = slot;
        this.rect = new ImmutableRect2i(slot.getRect());
        this.slot.setPosition(0, 0);
        ((RecipeSlotEx) this.slot).caa$setWidget(this);
        for (ProcessingOutput output: recipe.resultPool) {
            totalWeight += output.getChance();
        }
    }

    public static Couple<Long> fraction(double a, double b) {
        int scale = 0;
        while (scale < 4) {
            if (Math.abs(a - Math.round(a)) > 1e-5 || Math.abs(b - Math.round(b)) > 1e-5) {
                a *= 10;
                b *= 10;
                scale++;
            } else {
                break;
            }
        }
        long c = Math.round(a);
        long d = Math.round(b);
        while (d != 0) {
            long tmp = c % d;
            c = d;
            d = tmp;
        }
        return Couple.create(Math.round(a) / c, Math.round(b) / c);
    }

    @Override
    public @NotNull ScreenRectangle getArea() {
        return rect.toScreenRectangle();
    }

    @Override
    public @NotNull Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
        if (slot.isMouseOver(mouseX, mouseY))
            return Optional.of(new RecipeSlotUnderMouse(slot, rect.getX(), rect.getY()));
        return Optional.empty();
    }

    @Override
    public @NotNull ScreenPosition getPosition() {
        return rect.getScreenPosition();
    }

    @Override
    public void tick() {
        if (((RecipeSlotAccessor) slot).getDisplayIngredients() == null)
            return;
        if (Screen.hasShiftDown()) {
            ticker = 0;
            return;
        }
        ticker++;
        if (ticker >= 20) {
            ticker = 0;
            index++;
        }
        int size = ((RecipeSlotAccessor) slot).getDisplayIngredients().size();
        index = (index % size + size) % size;
    }

    @Override
    public void drawWidget(@NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        slot.draw(guiGraphics);
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (slot.isMouseOver(mouseX, mouseY))
            slot.getTooltip(tooltip);
    }

    @Override
    public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (scrollDelta == 0 || !slot.isMouseOver(mouseX, mouseY))
            return false;
        List<?> visible = ((RecipeSlotAccessor) slot).getDisplayIngredients();
        if (visible == null || recipe.resultPool.size() <= 2 || visible.size() <= 1)
            return false;
        if (scrollDelta > 0)
            index--;
        else
            index++;
        ticker = 0;
        int size = visible.size();
        index = (index % size + size) % size;
        return true;
    }
}
