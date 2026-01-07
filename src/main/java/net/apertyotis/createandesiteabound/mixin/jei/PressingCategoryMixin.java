package net.apertyotis.createandesiteabound.mixin.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.PressingCategory;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = PressingCategory.class, remap = false)
public abstract class PressingCategoryMixin extends CreateRecipeCategory<PressingRecipe> {
    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public PressingCategoryMixin(Info<PressingRecipe> info) {
        super(info);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull PressingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (mouseX > 70 && mouseX < 103 && mouseY > -1 && mouseY < 48) {
            tooltip.add(Component.translatable("jei.text.processing_duration", 10));
        }
        return tooltip;
    }
}
