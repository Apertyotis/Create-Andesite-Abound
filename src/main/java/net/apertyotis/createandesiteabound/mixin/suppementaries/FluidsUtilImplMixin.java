package net.apertyotis.createandesiteabound.mixin.suppementaries;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.utils.forge.FluidsUtilImpl;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FluidsUtilImpl.class, remap = false)
public abstract class FluidsUtilImplMixin {
    @WrapOperation(
        method = "fillFluidTank",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fluids/capability/IFluidHandler;fill(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)I"
        )
    )
    private static int accurateFilling(IFluidHandler handler, FluidStack fluid, IFluidHandler.FluidAction action, Operation<Integer> original) {
        if (handler.fill(fluid, IFluidHandler.FluidAction.SIMULATE) < fluid.getAmount())
            return 0;
        return original.call(handler, fluid, action);
    }
}
