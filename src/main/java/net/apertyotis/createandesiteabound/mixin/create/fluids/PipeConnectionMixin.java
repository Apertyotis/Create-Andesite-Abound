package net.apertyotis.createandesiteabound.mixin.create.fluids;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.FlowSource;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.OpenEndedPipe;
import com.simibubi.create.content.fluids.PipeConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(value = PipeConnection.class, remap = false)
public abstract class PipeConnectionMixin {
    @Shadow
    Optional<PipeConnection.Flow> flow;

    @Shadow
    Optional<FlowSource> source;

    @Shadow
    Optional<FlowSource> previousSource;

    // 略微降低管道过滤不必要的性能消耗
    @WrapOperation(
        method = "manageFlows",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/fluids/FlowSource;provideFluid(Ljava/util/function/Predicate;)Lnet/minecraftforge/fluids/FluidStack;",
            ordinal = 1
        )
    )
    private FluidStack checkExistingFlow(FlowSource flowSource, Predicate<FluidStack> filter, Operation<FluidStack> original) {
        if (flowSource instanceof FlowSource.OtherPipe)
            return original.call(flowSource, filter);
        // noinspection DataFlowIssue
        IFluidHandler handler = flowSource.provideHandler().orElse(null);
        // noinspection ConstantValue
        if (handler == null || flow.isEmpty())
            return FluidStack.EMPTY;
        FluidStack fluid = handler.drain(flow.get().fluid, IFluidHandler.FluidAction.SIMULATE);
        if (!filter.test(fluid))
            return FluidStack.EMPTY;
        return fluid;
    }

    // 增强流体网络的容错能力
    @Inject(method = "manageSource", at = @At("HEAD"))
    private void validateSource(Level world, BlockPos pos, CallbackInfo ci) {
        if (source.isPresent() && world.getGameTime() % 20 == 0) {
            Direction side = ((PipeConnection)(Object) this).side;
            BlockPos relative = pos.relative(side);
            if (world.getChunk(relative.getX() >> 4, relative.getZ() >> 4, ChunkStatus.FULL, false) == null)
                return;
            FlowSource flowSource = source.get();
            // 认为开口管道、连接到外部储罐的管道均为不可信任的
            if (flowSource instanceof OpenEndedPipe) {
                if (!FluidPropagator.isOpenEnd(world, pos, side)) {
                    previousSource = source;
                    source = Optional.empty();
                }
            } else if (flowSource instanceof FlowSource.FluidHandler) {
                if (!FluidPropagator.hasFluidCapability(world, relative, side.getOpposite())) {
                    previousSource = source;
                    source = Optional.empty();
                }
            }
        }
    }
}
