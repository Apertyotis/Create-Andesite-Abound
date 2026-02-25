package net.apertyotis.createandesiteabound.mixin.create.fluids;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.pipes.IAxisPipe;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
}
