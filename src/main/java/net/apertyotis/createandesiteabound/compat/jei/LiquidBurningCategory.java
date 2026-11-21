package net.apertyotis.createandesiteabound.compat.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import java.util.List;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.apertyotis.createandesiteabound.content.burner.LiquidBurningRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

public class LiquidBurningCategory extends CreateRecipeCategory<LiquidBurningRecipe> {
    private final AnimatedBlazeBurner burner = new AnimatedBlazeBurner();

    public LiquidBurningCategory(Info<LiquidBurningRecipe> info) {
        super(info);
    }

    @SuppressWarnings("removal")
    @Override
    @ParametersAreNonnullByDefault
    public void setRecipe(IRecipeLayoutBuilder builder, LiquidBurningRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> buckets = recipe.getFluidIngredient().getMatchingFluidStacks().stream()
            .map(fluid -> fluid.getFluid().getBucket())
            .distinct()
            .map(ItemStack::new)
            .toList();

        builder
            .addSlot(RecipeIngredientRole.INPUT, getBackground().getWidth() / 2 - 36, 3)
            .setBackground(getRenderedSlot(), -1, -1)
            .addItemStacks(buckets);

        builder
            .addSlot(RecipeIngredientRole.INPUT, getBackground().getWidth() / 2 - 16, 3)
            .setBackground(getRenderedSlot(), -1, -1)
            .addIngredients(ForgeTypes.FLUID_STACK, withImprovedVisibility(recipe.getFluidIngredient().getMatchingFluidStacks()))
            .addTooltipCallback(addFluidTooltip(recipe.getFluidIngredient().getRequiredAmount()));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void draw(LiquidBurningRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gg, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        HeatCondition requiredHeat = recipe.isSuperheated() ? HeatCondition.SUPERHEATED : HeatCondition.HEATED;

        AllGuiTextures.JEI_LIGHT.render(gg, 81, 58 + 30 - 50);

        AllGuiTextures.JEI_HEAT_BAR.render(gg, 4, 80 - 50);
        gg.drawString(font, Lang.translateDirect(requiredHeat.getTranslationKey()), 9, 86 - 50, requiredHeat.getColor());

        // 计算右对齐坐标
        String text = formatTime(recipe.getBurnTime());
        int xOffset = getBackground().getWidth() - font.width(text) - 8;
        gg.drawString(font, text, xOffset, 36, 4210752, false);

        burner.withHeat(requiredHeat.visualizeAsBlazeBurner())
            .draw(gg, getBackground().getWidth() / 2 + 3, 55 - 50);

        AllGuiTextures.JEI_DOWN_ARROW.render(gg, getBackground().getWidth() / 2 + 3, 8);
    }

    public String formatTime(int ticks) {
        String unit;
        if (ticks > 20) {
            ticks /= 20;
            unit = Component.translatable("caa.unit.sec").getString();
        } else {
            unit = Component.translatable("caa.unit.tick").getString();
        }

        return String.format("%d %s", ticks, unit);
    }
}
