package net.apertyotis.createandesiteabound.compat.createaddition;

import com.mrh0.createaddition.recipe.liquid_burning.LiquidBurningRecipe;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class LiquidBurningSuperHeat {
    private static List<ItemStack> buckets;

    public static List<ItemStack> getBuckets() {
        if (buckets == null) {
            buckets = new ArrayList<>();
            Level level = Minecraft.getInstance().level;
            if (level == null)
                return buckets;
            RecipeFinder.get(null, level, recipe -> {
                if (recipe instanceof LiquidBurningRecipe burningRecipe) {
                    return burningRecipe.isSuperheated();
                }
                return false;
            }).forEach(recipe -> {
                for (var ingredient: ((LiquidBurningRecipe) recipe).getFluidIngredient().getMatchingFluidStacks()) {
                    if (ingredient != null)
                        buckets.add(new ItemStack(ingredient.getFluid().getBucket()));
                }
            });
        }
        return buckets;
    }
}
