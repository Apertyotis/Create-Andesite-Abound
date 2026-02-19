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
    // 别惦记那个 b 无限流体了
    @Inject(method = "getStraightPipeAxis", at = @At("HEAD"), cancellable = true)
    private static void getMoreStraightPipeAxis(BlockState state, CallbackInfoReturnable<Direction.Axis> cir) {
        if (state.getBlock() instanceof IAxisPipe pipe) {
            cir.setReturnValue(pipe.getAxis(state));
        }
    }
}
