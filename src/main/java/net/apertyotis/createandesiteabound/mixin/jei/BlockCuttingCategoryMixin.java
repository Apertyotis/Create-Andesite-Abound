package net.apertyotis.createandesiteabound.mixin.jei;

import com.simibubi.create.compat.jei.category.BlockCuttingCategory;
import com.simibubi.create.compat.jei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.apertyotis.createandesiteabound.Config;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = BlockCuttingCategory.class, remap = false)
public abstract class BlockCuttingCategoryMixin extends CreateRecipeCategory<CondensedBlockCuttingRecipe> {
    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public BlockCuttingCategoryMixin(Info<CondensedBlockCuttingRecipe> info) {
        super(info);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull CondensedBlockCuttingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (mouseX > 25 && mouseX < 58 && mouseY > 26 && mouseY < 60) {
            int processingTicks = Config.saw_speed_change ? 10 : 50;
            tooltip.add(Component.translatable("jei.text.processing_duration", processingTicks));
        }
        return tooltip;
    }
}
