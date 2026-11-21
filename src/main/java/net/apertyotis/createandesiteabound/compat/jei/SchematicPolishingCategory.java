package net.apertyotis.createandesiteabound.compat.jei;

import com.simibubi.create.compat.jei.ConversionRecipe;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.apertyotis.createandesiteabound.AllItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("removal")
@ParametersAreNonnullByDefault
public class SchematicPolishingCategory extends CreateRecipeCategory<ConversionRecipe> {

    public static final List<ConversionRecipe> RECIPES = new ArrayList<>();

    static {
        RECIPES.add(ConversionRecipe.create(AllItems.SIMPLE_WRAPPER.asStack(), AllItems.SIMPLE_SCHEMATIC.asStack()));
    }

    private final ItemStack renderedWrapper;

    public SchematicPolishingCategory(Info<ConversionRecipe> info) {
        super(info);
        renderedWrapper = AllItems.SIMPLE_WRAPPER.asStack();
        CompoundTag tag = renderedWrapper.getOrCreateTag();
        tag.putBoolean("Polishing", true);
        tag.putBoolean("JEI", true);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ConversionRecipe recipe, IFocusGroup focuses) {
        builder
            .addSlot(RecipeIngredientRole.INPUT, 27, 29)
            .setBackground(getRenderedSlot(), -1, -1)
            .addIngredients(recipe.getIngredients().get(0));

        ProcessingOutput output = recipe.getRollableResults().get(0);
        builder
            .addSlot(RecipeIngredientRole.OUTPUT, 132, 29)
            .setBackground(getRenderedSlot(output), -1, -1)
            .addItemStack(output.getStack())
            .addTooltipCallback(addStochasticTooltip(output));
    }

    @Override
    public void draw(ConversionRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 61, 21);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 32);

        GuiGameElement.of(renderedWrapper)
            .<GuiGameElement.GuiRenderBuilder>at((float) (getBackground().getWidth() / 2) - 16, 0, 0)
            .scale(2)
            .render(graphics);
    }
}
