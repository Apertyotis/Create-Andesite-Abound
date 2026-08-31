package net.apertyotis.createandesiteabound.mixin.create.fluids;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.pipes.IAxisPipe;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FluidPropagator.class, remap = false)
public abstract class FluidPropagatorMixin {
    /**
     * 修复阀门和智能流体管道不正确更新流体网络的问题<br>
     * 详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/10001">#10001</a>
     */
    @Inject(method = "getStraightPipeAxis", at = @At("HEAD"), cancellable = true)
    private static void getMoreStraightPipeAxis(BlockState state, CallbackInfoReturnable<Direction.Axis> cir) {
        if (state.getBlock() instanceof IAxisPipe pipe) {
            cir.setReturnValue(pipe.getAxis(state));
        }
    }

    @WrapOperation(
        method = "resetAffectedFluidNetworks",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
            ordinal = 0
        )
    )
    private static boolean firstStep(
        List<?> frontier, Object start, Operation<Boolean> original,
        @Local(argsOnly = true) Direction side
    ) {
        // 由于网络传播更新时同时清除了到泵为止的 flow
        // 故原逻辑从泵后端开始的遍历会因为没有 flow 而停止
        if (AllConfig.pump_speed_change)
            start = ((BlockPos) start).relative(side);
        return original.call(frontier, start);
    }
}
