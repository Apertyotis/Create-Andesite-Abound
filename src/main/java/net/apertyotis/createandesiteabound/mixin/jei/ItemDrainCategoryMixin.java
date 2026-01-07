package net.apertyotis.createandesiteabound.mixin.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.ItemDrainCategory;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

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
}
