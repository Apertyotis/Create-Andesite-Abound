package net.apertyotis.createandesiteabound.mixin.create.fluids;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.simibubi.create.content.fluids.pipes.StraightPipeBlockEntity;
import net.apertyotis.createandesiteabound.foundation.FluidTransportBehaviourEx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidTransportBehaviour.class, remap = false)
public abstract class FluidTransportBehaviourMixin implements FluidTransportBehaviourEx {
    @Unique
    private BlockPos caa$filterPos;

    @Unique
    private boolean caa$attached = false;

    @Unique
    @Override
    public void caa$attachFilterPos(BlockPos pos) {
        BlockEntity be = ((FluidTransportBehaviour)(Object) this).blockEntity;
        if (be instanceof FluidPipeBlockEntity || be instanceof StraightPipeBlockEntity) {
            if (caa$attached) {
                if (caa$filterPos != null && !caa$filterPos.equals(pos))
                    caa$filterPos = null;
            } else {
                caa$filterPos = pos;
                caa$attached = true;
            }
        }
    }

    @Unique
    @Override
    public void caa$resetFilterPos() {
        caa$filterPos = null;
        caa$attached = false;
    }

    @Inject(method = "canPullFluidFrom", at = @At("RETURN"), cancellable = true)
    private void canPullFluidFromWithFilter(
        FluidStack fluid, BlockState state, Direction direction, CallbackInfoReturnable<Boolean> cir
    ) {
        BlockEntity be = ((FluidTransportBehaviour)(Object) this).blockEntity;
        Level level = be.getLevel();
        if (cir.getReturnValue() && caa$filterPos != null && level != null) {
            FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(level, caa$filterPos);
            if (pipeBehaviour != null)
                cir.setReturnValue(pipeBehaviour.canPullFluidFrom(fluid, state, direction));
        }
    }
}
