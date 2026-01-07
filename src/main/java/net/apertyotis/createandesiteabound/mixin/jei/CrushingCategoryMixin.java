package net.apertyotis.createandesiteabound.mixin.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.CrushingCategory;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = CrushingCategory.class, remap = false)
public abstract class CrushingCategoryMixin extends CreateRecipeCategory<AbstractCrushingRecipe> {
    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public CrushingCategoryMixin(Info<AbstractCrushingRecipe> info) {
        super(info);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull AbstractCrushingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (mouseX > 44 && mouseX < 132 && mouseY > 24 && mouseY < 71) {
            int duration = recipe.getProcessingDuration();
            tooltip.add(Component.translatable("jei.text.processing_duration", duration));
        }
        return tooltip;
    }
}
