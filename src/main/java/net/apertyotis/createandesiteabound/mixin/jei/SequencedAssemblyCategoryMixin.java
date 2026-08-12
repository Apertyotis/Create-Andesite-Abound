package net.apertyotis.createandesiteabound.mixin.jei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.SequencedAssemblyCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.utility.Couple;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.apertyotis.createandesiteabound.foundation.AssemblyJunkSlotWidget;
import net.apertyotis.createandesiteabound.foundation.RecipeSlotEx;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.ParametersAreNonnullByDefault;
import java.text.DecimalFormat;
import java.util.List;
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

    @SuppressWarnings("removal")
    @WrapOperation(
        method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/processing/sequenced/SequencedAssemblyRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
        at = @At(
            value = "INVOKE",
            target = "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;addTooltipCallback(Lmezz/jei/api/gui/ingredient/IRecipeSlotTooltipCallback;)Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;"
        )
    )
    private IRecipeSlotBuilder redirectAddSlotTooltipCallback(
        IRecipeSlotBuilder builder,
        mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback iRecipeSlotTooltipCallback,
        Operation<IRecipeSlotBuilder> original, @Local(argsOnly = true) SequencedAssemblyRecipe recipe
    ) {
        return builder.addTooltipCallback((recipeSlotView, tooltip) -> {
            if (recipe.resultPool.size() == 1)
                return;
            float chance = recipe.resultPool.get(0).getChance();
            float total = 0;
            for (ProcessingOutput output: recipe.resultPool) {
                total += output.getChance();
            }
            Couple<Long> frac = AssemblyJunkSlotWidget.fraction(chance, total);
            tooltip.add(1, chanceComponent(chance / total)
                .append(Component.literal(" (%d/%d)".formatted(frac.getFirst(), frac.getSecond()))
                    .withStyle(ChatFormatting.GRAY)));
        });
    }

    // 序列装配产物只有两种时，显示副产物槽，超过两种时，轮换显示
    @SuppressWarnings("removal")
    @Inject(
        method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/processing/sequenced/SequencedAssemblyRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
        at = @At("TAIL")
    )
    private void showDetailedByproduct(IRecipeLayoutBuilder builder, SequencedAssemblyRecipe recipe, IFocusGroup focuses, CallbackInfo ci) {
        if (recipe.resultPool.size() > 1) {
            builder
                .addSlot(RecipeIngredientRole.OUTPUT, 144, 91)
                .setBackground(CreateRecipeCategory.getRenderedSlot(0), -1, -1)
                .addItemStacks(recipe.resultPool.subList(1, recipe.resultPool.size())
                    .stream()
                    .map(ProcessingOutput::getStack)
                    .toList())
                .setSlotName("junk")
                .addTooltipCallback((recipeSlotView, tooltip) -> {
                    AssemblyJunkSlotWidget widget = ((RecipeSlotEx) recipeSlotView).caa$getWidget();
                    if (widget != null) {
                        float chance = widget.recipe.resultPool.get(widget.index + 1).getChance();
                        List<?> visible = ((RecipeSlotAccessor) widget.slot).getDisplayIngredients();
                        if (visible != null && visible.size() != widget.recipe.resultPool.size() - 1) {
                            ItemStack current = recipeSlotView.getDisplayedItemStack().orElse(ItemStack.EMPTY);
                            for (ProcessingOutput output: widget.recipe.resultPool) {
                                if (output.getStack().equals(current, true)) {
                                    chance = output.getChance();
                                    break;
                                }
                            }
                        }
                        Couple<Long> frac = AssemblyJunkSlotWidget.fraction(chance, widget.totalWeight);
                        tooltip.add(1, chanceComponent(chance / widget.totalWeight)
                            .append(Component.literal(" (%d/%d)".formatted(frac.getFirst(), frac.getSecond()))
                                .withStyle(ChatFormatting.GRAY)));
                        if ((visible == null && widget.recipe.resultPool.size() > 2) ||
                            (visible != null && visible.size() > 1)
                        ) {
                            tooltip.add(2, Component.translatable("jei.text.scroll_to_cycle")
                                .withStyle(ChatFormatting.DARK_GRAY));
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

    @Override
    @ParametersAreNonnullByDefault
    public void createRecipeExtras(IRecipeExtrasBuilder builder, SequencedAssemblyRecipe recipe, IFocusGroup focuses) {
        Optional<IRecipeSlotDrawable> junkSlot = builder.getRecipeSlots().findSlotByName("junk");
        if (junkSlot.isEmpty())
            return;

        AssemblyJunkSlotWidget widget = new AssemblyJunkSlotWidget(recipe, junkSlot.get());
        builder.addSlottedWidget(widget, List.of(junkSlot.get()));
        builder.addInputHandler(widget);
    }
}
