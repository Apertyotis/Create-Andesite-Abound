package net.apertyotis.createandesiteabound.mixin.createaddition;

import com.mrh0.createaddition.compat.jei.CARecipeCategory;
import com.mrh0.createaddition.compat.jei.RollingMillCategory;
import com.mrh0.createaddition.recipe.rolling.RollingRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = RollingMillCategory.class, remap = false)
public abstract class RollingMillCategoryMixin extends CARecipeCategory<RollingRecipe> {

    public RollingMillCategoryMixin(Info<RollingRecipe> info) {
        super(info);
    }

    @SuppressWarnings("removal")
    @Override
    @ParametersAreNonnullByDefault
    public @NotNull List<Component> getTooltipStrings(RollingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (mouseX > 45 && mouseX < 75 && mouseY > 21 && mouseY < 52) {
            int duration = recipe.getProcessingDuration();
            tooltip.add(Component.translatable("jei.text.processing_duration", duration));
        }
        return tooltip;
    }
}
