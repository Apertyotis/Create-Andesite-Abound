package net.apertyotis.createandesiteabound.mixin.createdieselgenerators;

import com.jesz.createdieselgenerators.compat.jei.BasinFermentingCategory;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.apertyotis.createandesiteabound.content.burner.LiquidBurningSuperHeat;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = BasinFermentingCategory.class, remap = false)
public abstract class BasinFermentingCategoryMixin extends CreateRecipeCategory<BasinRecipe> {

    public BasinFermentingCategoryMixin(Info<BasinRecipe> info) {
        super(info);
    }

    @WrapOperation(
        method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/processing/basin/BasinRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
        at = @At(
            value = "INVOKE",
            target = "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;addItemStack(Lnet/minecraft/world/item/ItemStack;)Lmezz/jei/api/gui/builder/IIngredientAcceptor;",
            ordinal = 2
        )
    )
    private IIngredientAcceptor<IRecipeSlotBuilder> redirectSuperHeatCatalyst(
        IRecipeSlotBuilder builder, ItemStack stack, Operation<IIngredientAcceptor<IRecipeSlotBuilder>> original
    ) {
        return LiquidBurningSuperHeat.appendSuperHeatCatalyst(builder, stack);
    }

    @SuppressWarnings("removal")
    @Override
    @ParametersAreNonnullByDefault
    public @NotNull List<Component> getTooltipStrings(BasinRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if(mouseX > 90 && mouseX < 121 && mouseY > 41 && mouseY < 73) {
            int duration = recipe.getProcessingDuration();
            tooltip.add(Component.translatable("jei.text.processing_duration", duration));
        }
        return tooltip;
    }
}
