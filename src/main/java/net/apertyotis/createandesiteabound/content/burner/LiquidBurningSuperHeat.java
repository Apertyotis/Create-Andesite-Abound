package net.apertyotis.createandesiteabound.content.burner;

import com.simibubi.create.compat.jei.CreateJEI;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import net.apertyotis.createandesiteabound.AllRecipeTypes;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LiquidBurningSuperHeat {
    private static List<ItemStack> getBuckets() {
        return CreateJEI.getTypedRecipes(AllRecipeTypes.LIQUID_BURNING.type()).stream()
            .filter(recipe -> recipe instanceof LiquidBurningRecipe burningRecipe && burningRecipe.isSuperheated())
            .flatMap(recipe -> ((LiquidBurningRecipe) recipe).getFluidIngredient().getMatchingFluidStacks().stream())
            .map(fluid -> fluid.getFluid().getBucket())
            .distinct()
            .map(ItemStack::new)
            .toList();
    }

    public static IIngredientAcceptor<IRecipeSlotBuilder> appendSuperHeatCatalyst(IRecipeSlotBuilder builder, ItemStack stack) {
        List<ItemStack> liquidCatalysts = getBuckets();
        if (liquidCatalysts.isEmpty()) {
            return builder.addItemStack(stack);
        } else {
            List<ItemStack> superHeatCatalysts = new ArrayList<>();
            superHeatCatalysts.add(stack);
            superHeatCatalysts.addAll(liquidCatalysts);
            return builder.addItemStacks(superHeatCatalysts);
        }
    }
}
