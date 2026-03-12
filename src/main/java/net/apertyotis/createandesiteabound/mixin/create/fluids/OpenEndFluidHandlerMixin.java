package net.apertyotis.createandesiteabound.mixin.create.fluids;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.OpenEndedPipe;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// 注入私有内部类
@Mixin(targets = "com.simibubi.create.content.fluids.OpenEndedPipe$OpenEndFluidHandler", remap = false)
public abstract class OpenEndFluidHandlerMixin {
    @WrapOperation(
            method = "fill",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/fluids/OpenEndedPipe;" +
                            "provideFluidToSpace(Lnet/minecraftforge/fluids/FluidStack;Z)Z",
                    ordinal = 1
            )
    )
    private boolean provideUpdatedFluidToSpace(
            OpenEndedPipe instance, FluidStack fluid, boolean simulate, Operation<Boolean> original
    ) {
        FluidStack containedFluidStack = ((IFluidTank) this).getFluid();
        boolean hasBlockState = FluidHelper.hasBlockState(containedFluidStack.getFluid());
        if (((IFluidTank) this).getFluidAmount() == 1000 || !hasBlockState) {
            return original.call(instance, containedFluidStack, simulate);
        } else {
            return false;
        }
    }
}
