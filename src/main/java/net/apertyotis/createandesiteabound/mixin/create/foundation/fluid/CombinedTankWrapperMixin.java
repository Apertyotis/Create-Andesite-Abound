package net.apertyotis.createandesiteabound.mixin.create.foundation.fluid;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CombinedTankWrapper.class, remap = false)
public abstract class CombinedTankWrapperMixin {
    // 目标函数极难获取准确的 int 局部变量，因此改用 share 在变量值产生时存储
    @WrapOperation(
            method = "fill",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/fluids/capability/IFluidHandler;fill(" +
                            "Lnet/minecraftforge/fluids/FluidStack;" +
                            "Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)I"
            )
    )
    private int localSaveFilledIntoCurrent(
            IFluidHandler instance,
            FluidStack fluidStack,
            IFluidHandler.FluidAction fluidAction,
            Operation<Integer> original,
            @Share("filledIntoCurrent") LocalIntRef localIntRef)
    {
        int value = original.call(instance, fluidStack, fluidAction);
        localIntRef.set(value);
        return value;
    }

    /**
     * 修复空工作盆接受速度足够快的泵输入时，可以让不同槽位容纳同种液体的问题<br>
     * 详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/9974">#9974</a>
     */
    @ModifyVariable(
            method = "fill",
            at = @At(value = "LOAD"),
            name = "fittingHandlerFound",
            slice = @Slice(from = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/fluids/capability/IFluidHandler;fill(" +
                            "Lnet/minecraftforge/fluids/FluidStack;" +
                            "Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)I"
            ))
    )
    private boolean correctVariety(
            boolean fittingHandlerFound,
            @Share("filledIntoCurrent") LocalIntRef localIntRef,
            @Cancellable CallbackInfoReturnable<Integer> cir)
    {
        boolean enforceVariety = ((CombinedTankWrapperAccessor) this).isEnforceVariety();
        int filledIntoCurrent = localIntRef.get();
        if (enforceVariety && (fittingHandlerFound || filledIntoCurrent != 0)) {
            cir.setReturnValue(filledIntoCurrent);
        }
        return false;
    }
}
