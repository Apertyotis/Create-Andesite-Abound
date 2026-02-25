package net.apertyotis.createandesiteabound.mixin.create.fluids.hosePulley;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyFluidHandler;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = HosePulleyFluidHandler.class, remap = false)
public abstract class HosePulleyFluidHandlerMixin {
    // 修复软管滑轮一次注入大于 1000mB 时的奇怪行为
    @ModifyArg(
            method = "fill",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/fluid/SmartFluidTank;fill(" +
                            "Lnet/minecraftforge/fluids/FluidStack;" +
                            "Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)I",
                    ordinal = 0
            ),
            index = 0
    )
    private FluidStack redirectSimulateFill(FluidStack fluid, @Local(argsOnly = true) FluidStack resource) {
        return resource;
    }
}