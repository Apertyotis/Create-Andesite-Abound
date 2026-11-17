package net.apertyotis.createandesiteabound.mixin.createdieselgenerators;

import com.jesz.createdieselgenerators.compat.jei.AnimatedDistillationTower;
import com.jesz.createdieselgenerators.compat.jei.DistillationCategory;
import com.jesz.createdieselgenerators.recipes.DistillationRecipe;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = DistillationCategory.class, remap = false)
public abstract class DistillationCategoryMixin extends CreateRecipeCategory<DistillationRecipe> {

    public DistillationCategoryMixin(Info<DistillationRecipe> info) {
        super(info);
    }

    @WrapOperation(
        method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/jesz/createdieselgenerators/recipes/DistillationRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
        at = @At(
            value = "INVOKE",
            target = "Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;addSlot(Lmezz/jei/api/recipe/RecipeIngredientRole;II)Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;"
        )
    )
    private IRecipeSlotBuilder resize1(IRecipeLayoutBuilder instance, RecipeIngredientRole role, int x, int y, Operation<IRecipeSlotBuilder> original) {
        return original.call(instance, role, x, y - 44);
    }

    @WrapOperation(
        method = "draw(Lcom/jesz/createdieselgenerators/recipes/DistillationRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/foundation/gui/AllGuiTextures;render(Lnet/minecraft/client/gui/GuiGraphics;II)V"
        )
    )
    private void resize2(AllGuiTextures instance, GuiGraphics graphics, int x, int y, Operation<Void> original) {
        original.call(instance, graphics, x, y - 44);
    }

    @WrapOperation(
        method = "draw(Lcom/jesz/createdieselgenerators/recipes/DistillationRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/jesz/createdieselgenerators/compat/jei/AnimatedDistillationTower;draw(Lnet/minecraft/client/gui/GuiGraphics;III)V"
        )
    )
    private void resize3(AnimatedDistillationTower instance, GuiGraphics graphics, int x, int y, int h, Operation<Void> original) {
        original.call(instance, graphics, x, y - 44, h);
    }

    @WrapOperation(
        method = "draw(Lcom/jesz/createdieselgenerators/recipes/DistillationRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/compat/jei/category/animations/AnimatedBlazeBurner;draw(Lnet/minecraft/client/gui/GuiGraphics;II)V"
        )
    )
    private void resize4(AnimatedBlazeBurner instance, GuiGraphics graphics, int x, int y, Operation<Void> original) {
        original.call(instance, graphics, x, y - 44);
    }

    @WrapOperation(
        method = "draw(Lcom/jesz/createdieselgenerators/recipes/DistillationRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I",
            remap = true
        )
    )
    private int resize5(GuiGraphics instance, Font font, Component text, int x, int y, int color, boolean shadow, Operation<Integer> original) {
        return original.call(instance, font, text, x, y - 44, color, shadow);
    }

    @SuppressWarnings("removal")
    @Override
    @ParametersAreNonnullByDefault
    public @NotNull List<Component> getTooltipStrings(DistillationRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        int height = recipe.getFluidResults().size() + 1;
        if(mouseX > 90 && mouseX < 121 && mouseY > 117 - 22 * height && mouseY < 125) {
            int duration = recipe.getProcessingDuration();
            tooltip.add(Component.translatable("jei.text.processing_duration", duration));
        }
        return tooltip;
    }
}
