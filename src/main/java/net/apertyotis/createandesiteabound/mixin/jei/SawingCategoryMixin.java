package net.apertyotis.createandesiteabound.mixin.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.SawingCategory;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.apertyotis.createandesiteabound.Config;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = SawingCategory.class, remap = false)
public abstract class SawingCategoryMixin extends CreateRecipeCategory<CuttingRecipe> {
    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public SawingCategoryMixin(Info<CuttingRecipe> info) {
        super(info);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull CuttingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (mouseX > 63 && mouseX < 97 && mouseY > 31 && mouseY < 65) {
            int processingTicks;
            if (Config.saw_speed_change) {
                processingTicks = 10;
            } else {
                processingTicks = recipe.getProcessingDuration();
            }
            tooltip.add(Component.translatable("jei.text.processing_duration", processingTicks));
        }
        return tooltip;
    }
}
