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

import java.util.Optional;

@Mixin(value = BasinCategory.class, remap = false)
public abstract class BasinCategoryMixin {
    // 存在 Create Craft&Addition 时，添加超级加热燃料到烈焰蛋糕的显示
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
        Optional<IIngredientAcceptor<IRecipeSlotBuilder>> result = Mods.CreateAddition.runIfInstalled(
                () -> () -> LiquidBurningSuperHeat.appendSuperHeatCatalyst(builder, stack));
        if (result.isPresent())
            return result.get();
        return original.call(builder, stack);
    }
}
