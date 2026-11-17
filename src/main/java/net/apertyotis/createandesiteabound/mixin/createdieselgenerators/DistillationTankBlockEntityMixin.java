package net.apertyotis.createandesiteabound.mixin.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.entity.DistillationTankBlockEntity;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Predicate;

@Mixin(value = DistillationTankBlockEntity.class, remap = false)
public abstract class DistillationTankBlockEntityMixin {

    @Unique
    private static final Object RECIPE_CACHE_KEY = new Object();

    @Definition(id = "width", field = "Lcom/jesz/createdieselgenerators/blocks/entity/DistillationTankBlockEntity;width:I")
    @Expression("?.width * ?.width")
    @ModifyExpressionValue(method = "tick", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 1))
    private int correctProgress(int original) {
        return 1;
    }

    @WrapOperation(
        method = "getMatchingRecipes",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/foundation/recipe/RecipeFinder;get(Ljava/lang/Object;Lnet/minecraft/world/level/Level;Ljava/util/function/Predicate;)Ljava/util/List;"
        )
    )
    private List<Recipe<?>> fixMemoryLeak(Object cachedKey, Level world, Predicate<Recipe<?>> conditions, Operation<List<Recipe<?>>> original) {
        return original.call(RECIPE_CACHE_KEY, world, conditions);
    }
}
