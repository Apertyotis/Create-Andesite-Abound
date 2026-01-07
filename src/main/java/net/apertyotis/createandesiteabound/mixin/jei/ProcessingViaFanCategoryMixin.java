package net.apertyotis.createandesiteabound.mixin.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.ProcessingViaFanCategory;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ProcessingViaFanCategory.class, remap = false)
public abstract class ProcessingViaFanCategoryMixin<T extends Recipe<?>> extends CreateRecipeCategory<T> {
    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public ProcessingViaFanCategoryMixin(Info<T> info) {
        super(info);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull T recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (mouseX > 55 && mouseX < 106 && mouseY > 8 && mouseY < 48) {
            tooltip.add(Component.translatable("jei.text.processing_duration_with_num", 150, 16));
        }
        return tooltip;
    }
}
