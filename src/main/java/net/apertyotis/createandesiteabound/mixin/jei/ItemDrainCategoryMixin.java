package net.apertyotis.createandesiteabound.mixin.jei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.ItemDrainCategory;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ItemDrainCategory.class, remap = false)
public abstract class ItemDrainCategoryMixin extends CreateRecipeCategory<EmptyingRecipe> {
    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public ItemDrainCategoryMixin(Info<EmptyingRecipe> info) {
        super(info);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull EmptyingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (mouseX > 74 && mouseX < 102 && mouseY > 21 && mouseY < 45) {
            tooltip.add(Component.translatable("jei.text.processing_duration", 10));
        }
        return tooltip;
    }

    // 让分液池能显示产物概率
    @WrapOperation(
            method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/fluids/transfer/EmptyingRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;addItemStack(Lnet/minecraft/world/item/ItemStack;)Lmezz/jei/api/gui/builder/IIngredientAcceptor;"
            )
    )
    private IIngredientAcceptor<IRecipeSlotBuilder> attachChanceTooltip(
            IRecipeSlotBuilder instance, ItemStack itemStack,
            Operation<IRecipeSlotBuilder> original,
            @Local(argsOnly = true) EmptyingRecipe recipe)
    {
        return original.call(instance, itemStack)
                .addTooltipCallback(addStochasticTooltip(recipe.getRollableResults().get(0)));
    }

    // 让分液池显示概率产物的槽位背景
    @WrapOperation(
            method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/fluids/transfer/EmptyingRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;setBackground(Lmezz/jei/api/gui/drawable/IDrawable;II)Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;" ,
                    ordinal = 2
            )
    )
    private IRecipeSlotBuilder redirectSetBackground(
            IRecipeSlotBuilder instance,
            IDrawable iDrawable,
            int x, int y,
            Operation<IRecipeSlotBuilder> original,
            @Local(argsOnly = true) EmptyingRecipe recipe)
    {
        return instance.setBackground(getRenderedSlot(recipe.getRollableResults().get(0)), x, y);
    }
}
