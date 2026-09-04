package net.apertyotis.createandesiteabound.mixin.create.fluids.hosePulley;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.transfer.FluidFillingBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "canBeReplacedByFluid", at = @At("HEAD"), cancellable = true)
    private void protectBE(BlockGetter world, BlockPos pos, BlockState pState, CallbackInfoReturnable<Boolean> cir) {
        if (pState.hasBlockEntity())
            cir.setReturnValue(false);
    }
}
