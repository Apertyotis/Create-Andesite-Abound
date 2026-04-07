package net.apertyotis.createandesiteabound.mixin.create.fluids.hosePulley;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.transfer.FluidFillingBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(value = FluidFillingBehaviour.class, remap = false)
public abstract class FluidFillingBehaviourMixin {
    // 无限阈值实际为设定值 + 1，因此修改判定，使得软管滑轮可以多放置一格液体
    @Definition(id = "size", method = "Ljava/util/Set;size()I")
    @Expression("?.size() >= ?")
    @ModifyExpressionValue(method = "tryDeposit", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean preventNotFillingLastTwoFluidState(boolean original, @Local(name = "maxBlocks") int maxBlocks) {
        return ((FluidManipulationBehaviourAccessor) this).getVisited().size() > maxBlocks;
    }

    // 取消第一次visited::add，使得软管滑轮可以放置最后一格液体
    @WrapOperation(
            method = "tryDeposit",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", ordinal = 0)
    )
    private boolean cancelFirstAdd(Set<?> instance, Object e, Operation<Boolean> original) {
        return false;
    }
}
