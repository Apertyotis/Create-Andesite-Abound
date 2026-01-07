package net.apertyotis.createandesiteabound.mixin.jei;

import com.simibubi.create.compat.jei.category.BasinCategory;
import com.simibubi.create.compat.jei.category.PackingCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = PackingCategory.class, remap = false)
public abstract class PackingCategoryMixin extends BasinCategory {
    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public PackingCategoryMixin(Info<BasinRecipe> info, boolean needsHeating) {
        super(info, needsHeating);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull BasinRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (mouseX > 90 && mouseX < 121 && mouseY > 10 && mouseY < 76) {
            tooltip.add(Component.translatable("jei.text.processing_duration", 10));
        }
        return tooltip;
    }
}
