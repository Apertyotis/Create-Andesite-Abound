package net.apertyotis.createandesiteabound.mixin.jei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.compat.jei.category.BasinCategory;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import net.apertyotis.createandesiteabound.compat.Mods;
import net.apertyotis.createandesiteabound.compat.createaddition.LiquidBurningSuperHeat;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(value = BasinCategory.class, remap = false)
public abstract class BasinCategoryMixin {
    @WrapOperation(
            method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/processing/basin/BasinRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;addItemStack(Lnet/minecraft/world/item/ItemStack;)Lmezz/jei/api/gui/builder/IIngredientAcceptor;",
                    ordinal = 2
            )
    )
    private IIngredientAcceptor<IRecipeSlotBuilder> redirectSuperHeatCatalyst(
            IRecipeSlotBuilder builder,
            ItemStack stack,
            Operation<IIngredientAcceptor<IRecipeSlotBuilder>> original
    ) {
        Optional<List<ItemStack>> buckets = Mods.createaddition.runIfInstalled(() -> LiquidBurningSuperHeat::getBuckets);
        if (buckets.isPresent() && !buckets.get().isEmpty()) {
            List<ItemStack> superHeatCatalyst = new ArrayList<>();
            superHeatCatalyst.add(stack);
            superHeatCatalyst.addAll(buckets.get());
            return builder.addItemStacks(superHeatCatalyst);
        }
        return original.call(builder, stack);
    }
}
