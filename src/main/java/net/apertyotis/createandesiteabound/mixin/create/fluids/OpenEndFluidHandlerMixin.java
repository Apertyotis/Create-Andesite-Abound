package net.apertyotis.createandesiteabound.mixin.create.fluids;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// 注入私有内部类
@Mixin(targets = "com.simibubi.create.content.fluids.OpenEndedPipe$OpenEndFluidHandler", remap = false)
public abstract class OpenEndFluidHandlerMixin {
    // 修复一次性向世界排出 1000mB 液体不会正确放置液体方块的问题
    @ModifyArg(
            method = "fill",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/fluids/OpenEndedPipe;" +
                            "provideFluidToSpace(Lnet/minecraftforge/fluids/FluidStack;Z)Z",
                    ordinal = 1
            ),
            index = 0
    )
    private FluidStack updateContainedFluidStack(FluidStack fluid) {
        return ((IFluidTank) this).getFluid();
    }
}
