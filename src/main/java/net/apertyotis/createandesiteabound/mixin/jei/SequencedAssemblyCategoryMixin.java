package net.apertyotis.createandesiteabound.mixin.jei;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.SequencedAssemblyCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;
import java.util.Optional;

@Mixin(value = SequencedAssemblyCategory.class, remap = false)
public abstract class SequencedAssemblyCategoryMixin extends CreateRecipeCategory<SequencedAssemblyRecipe> {

    @Shadow
    protected abstract MutableComponent chanceComponent(float chance);

    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public SequencedAssemblyCategoryMixin(Info<SequencedAssemblyRecipe> info) {
        super(info);
    }

    // 序列装配的产物概率别显示<1和>99
    @ModifyVariable(
            method = "chanceComponent",
            at = @At(value = "STORE"),
            name = "number"
    )
    private String redirectNumberString(String number, @Local(argsOnly = true) float chance) {
        DecimalFormat df = new DecimalFormat("0.####");
        return df.format(chance * 100);
    }

    // 序列装配产物只有两种时，显示副产物槽，超过两种时，轮换显示
    @Inject(
            method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/processing/sequenced/SequencedAssemblyRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At("TAIL")
    )
    private void showDetailedByproduct(IRecipeLayoutBuilder builder, SequencedAssemblyRecipe recipe, IFocusGroup focuses, CallbackInfo ci) {
        if (recipe.resultPool.size() > 1) {
            float totalWeight = 0;
            for (ProcessingOutput entry : recipe.resultPool)
                totalWeight += entry.getChance();
            float finalTotalWeight = totalWeight;
            builder
                    .addSlot(RecipeIngredientRole.OUTPUT, 144, 91)
                    .setBackground(getRenderedSlot(0), -1, -1)
                    .addItemStacks(recipe.resultPool.subList(1, recipe.resultPool.size())
                            .stream()
                            .map(ProcessingOutput::getStack)
                            .toList()
                    )
                    .addTooltipCallback((recipeSlotView, tooltip) -> {
                        Optional<ItemStack> stack = recipeSlotView.getDisplayedItemStack();
                        if (stack.isEmpty() || stack.get().isEmpty()) return;

                        for (ProcessingOutput entry: recipe.resultPool.subList(1, recipe.resultPool.size())) {
                            if (entry.getStack().equals(stack.get(), true)) {
                                tooltip.add(1, chanceComponent(entry.getChance() / finalTotalWeight));
                                break;
                            }
                        }
                    });
        }
    }

    // 有多种装配产物时，无需再另外绘制随机废料的问号槽
    @ModifyVariable(
            method = "draw(Lcom/simibubi/create/content/processing/sequenced/SequencedAssemblyRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V",
            at = @At(value = "LOAD", ordinal = 1),
            name = "singleOutput"
    )
    private boolean cancelDrawByproduct(boolean singleOutput, @Local(argsOnly = true) SequencedAssemblyRecipe recipe) {
        return singleOutput || recipe.resultPool.size() > 1;
    }

    // 有多种装配产物时，也无需再另外添加随机废料的tooltip
    @ModifyVariable(
            method = "getTooltipStrings(Lcom/simibubi/create/content/processing/sequenced/SequencedAssemblyRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;DD)Ljava/util/List;",
            at = @At(value = "LOAD"),
            name = "singleOutput"
    )
    private boolean cancelAddByproductTooltip(boolean singleOutput, @Local(argsOnly = true) SequencedAssemblyRecipe recipe) {
        return singleOutput || recipe.resultPool.size() > 1;
    }

}
