package net.apertyotis.createandesiteabound.mixin.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.utility.Lang;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.DecimalFormat;

@Mixin(value = CreateRecipeCategory.class, remap = false)
public abstract class CreateRecipeCategoryMixin<T extends Recipe<?>> implements IRecipeCategory<T> {
    // 别显示<1%了
    @Inject(method = "addStochasticTooltip", at = @At("HEAD"), cancellable = true)
    private static void redirectTooltipCallback(ProcessingOutput output, CallbackInfoReturnable<IRecipeSlotTooltipCallback> cir) {
        cir.setReturnValue((view, tooltip) -> {
            float chance = output.getChance();
            if (chance != 1) {
                DecimalFormat df = new DecimalFormat("0.#####");
                String chanceString = df.format(chance * 100);
                tooltip.add(1, Lang.translateDirect("recipe.processing.chance", chanceString)
                        .withStyle(ChatFormatting.GOLD));
            }
        });
    }
}
