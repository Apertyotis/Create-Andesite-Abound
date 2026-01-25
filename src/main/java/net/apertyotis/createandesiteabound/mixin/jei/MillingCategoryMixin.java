package net.apertyotis.createandesiteabound.mixin.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.MillingCategory;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.apertyotis.createandesiteabound.Config;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = MillingCategory.class, remap = false)
public abstract class MillingCategoryMixin extends CreateRecipeCategory<AbstractCrushingRecipe> {
    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public MillingCategoryMixin(Info<AbstractCrushingRecipe> info) {
        super(info);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull AbstractCrushingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (mouseX > 42 && mouseX < 71 && mouseY > 18 && mouseY < 50) {
            int duration = recipe.getProcessingDuration();
            if (duration == 0) duration = Config.millstone_speed_change ? 10 : 100;
            tooltip.add(Component.translatable("jei.text.processing_duration", duration));
        }
        return tooltip;
    }
}
