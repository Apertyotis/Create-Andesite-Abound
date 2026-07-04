package net.apertyotis.createandesiteabound.mixin.create.fluids;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.FlowSource;
import com.simibubi.create.content.fluids.PipeConnection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(value = PipeConnection.class, remap = false)
public abstract class PipeConnectionMixin {
    @Shadow
    Optional<PipeConnection.Flow> flow;

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
}
