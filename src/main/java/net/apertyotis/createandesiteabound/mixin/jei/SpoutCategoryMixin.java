package net.apertyotis.createandesiteabound.mixin.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.SpoutCategory;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = SpoutCategory.class, remap = false)
public abstract class SpoutCategoryMixin extends CreateRecipeCategory<FillingRecipe> {
    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public SpoutCategoryMixin(Info<FillingRecipe> info) {
        super(info);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull FillingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (mouseX > 76 && mouseX < 100 && mouseY > 1 && mouseY < 64) {
            tooltip.add(Component.translatable("jei.text.processing_duration", 20));
        }
        return tooltip;
    }
}
