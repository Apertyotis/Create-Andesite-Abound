package net.apertyotis.createandesiteabound.mixin.createaddition;

import com.mrh0.createaddition.compat.jei.CARecipeCategory;
import com.mrh0.createaddition.compat.jei.ChargingCategory;
import com.mrh0.createaddition.config.Config;
import com.mrh0.createaddition.recipe.charging.ChargingRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = ChargingCategory.class, remap = false)
public abstract class ChargingCategoryMixin extends CARecipeCategory<ChargingRecipe> {
    public ChargingCategoryMixin(Info<ChargingRecipe> info) {
        super(info);
    }

    @SuppressWarnings("removal")
    @Override
    @ParametersAreNonnullByDefault
    public @NotNull List<Component> getTooltipStrings(ChargingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (mouseX > 84 && mouseX < 126 && mouseY > 7 && mouseY < 45) {
            int total = recipe.getEnergy();
            int rate = Math.min(recipe.getMaxChargeRate(), Config.TESLA_COIL_RECIPE_CHARGE_RATE.get());
            tooltip.add(Component.translatable("jei.text.total_energy", total));
            tooltip.add(Component.translatable("jei.text.max_charge_rate", rate));
        }
        return tooltip;
    }
}
