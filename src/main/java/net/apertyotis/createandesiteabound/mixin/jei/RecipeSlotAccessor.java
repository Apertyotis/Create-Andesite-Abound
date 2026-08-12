package net.apertyotis.createandesiteabound.mixin.jei;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Optional;

@Mixin(value = RecipeSlot.class, remap = false)
public interface RecipeSlotAccessor {
    @Accessor("displayIngredients")
    List<Optional<ITypedIngredient<?>>> getDisplayIngredients();
}
