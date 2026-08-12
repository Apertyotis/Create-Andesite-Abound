package net.apertyotis.createandesiteabound.mixin.jei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import mezz.jei.library.gui.ingredients.TagContentTooltipComponent;
import net.apertyotis.createandesiteabound.foundation.AssemblyContentTooltipComponent;
import net.apertyotis.createandesiteabound.foundation.AssemblyJunkSlotWidget;
import net.apertyotis.createandesiteabound.foundation.RecipeSlotEx;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(value = RecipeSlot.class, remap = false)
public abstract class RecipeSlotMixin implements RecipeSlotEx {
    @Shadow
    @Unmodifiable
    private @Nullable List<Optional<ITypedIngredient<?>>> displayIngredients;

    @Unique
    private AssemblyJunkSlotWidget caa$widget;

    @Unique
    @Override
    public void caa$setWidget(AssemblyJunkSlotWidget widget) {
        caa$widget = widget;
    }

    @Unique
    @Override
    public AssemblyJunkSlotWidget caa$getWidget() {
        return caa$widget;
    }

    @Inject(method = "getDisplayedIngredient", at = @At("TAIL"), cancellable = true)
    private void redirectGetDisplayedIngredient(CallbackInfoReturnable<Optional<ITypedIngredient<?>>> cir) {
        if (caa$widget != null && displayIngredients != null) {
            cir.setReturnValue(displayIngredients.get(caa$widget.index));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @WrapOperation(
        method = "addIngredientsToTooltip",
        at = @At(
            value = "NEW",
            target = "(Lmezz/jei/api/ingredients/IIngredientRenderer;Ljava/util/List;)Lmezz/jei/library/gui/ingredients/TagContentTooltipComponent;"
        )
    )
    private TagContentTooltipComponent redirectClientTooltipComponent(
        IIngredientRenderer renderer, List ingredients, Operation<TagContentTooltipComponent> original
    ) {
        if (caa$widget == null)
            return original.call(renderer, ingredients);
        return new AssemblyContentTooltipComponent(renderer, ingredients, caa$widget);
    }
}
