package net.apertyotis.createandesiteabound.mixin.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.DeployingCategory;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = DeployingCategory.class, remap = false)
public abstract class DeployingCategoryMixin extends CreateRecipeCategory<DeployerApplicationRecipe> {
    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public DeployingCategoryMixin(Info<DeployerApplicationRecipe> info) {
        super(info);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull DeployerApplicationRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (mouseX > 74 && mouseX < 102 && mouseY > 1 && mouseY < 65) {
            tooltip.add(Component.translatable("jei.text.processing_duration", 5));
        }
        return tooltip;
    }
}
