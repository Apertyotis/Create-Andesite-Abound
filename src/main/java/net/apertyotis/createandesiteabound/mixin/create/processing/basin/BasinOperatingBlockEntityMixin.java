package net.apertyotis.createandesiteabound.mixin.create.processing.basin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Comparator;
import java.util.stream.Stream;

@Mixin(value = BasinOperatingBlockEntity.class, remap = false)
public abstract class BasinOperatingBlockEntityMixin {
    // 修复盆加工类机器按原料数量比较配方优先级时未考虑流体原料的问题
    @WrapOperation(
            method = "getMatchingRecipes",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;sorted(Ljava/util/Comparator;)Ljava/util/stream/Stream;"
            )
    )
    private Stream<Recipe<?>> redirectSortComparator(
            Stream<Recipe<?>> instance,
            Comparator<? super Recipe<?>> comparator,
            Operation<Stream<Recipe<?>>> original)
    {
        return instance.sorted((r1, r2) -> {
            int size1 = r1.getIngredients().size();
            int size2 = r2.getIngredients().size();
            if (r1 instanceof ProcessingRecipe<?> pr1) {
                size1 += pr1.getFluidIngredients().size();
            }
            if (r2 instanceof ProcessingRecipe<?> pr2) {
                size2 += pr2.getFluidIngredients().size();
            }
            return size2 - size1;
        });
    }
}
