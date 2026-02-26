package net.apertyotis.createandesiteabound.mixin.create.fluids;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.OpenEndedPipe;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = OpenEndedPipe.class, remap = false)
public abstract class OpenEndedPipeMixin {
    /**
     * 修复动力泵无法替换放置流动液体方块的问题<br>
     * 另一种修复方案见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/7054">#7054</a>
     */
    @SuppressWarnings("DefaultAnnotationParam")
    @Definition(id = "getType", method = "Lnet/minecraft/world/level/material/FluidState;getType()Lnet/minecraft/world/level/material/Fluid;", remap = true)
    @Definition(id = "getFluid", method = "Lnet/minecraftforge/fluids/FluidStack;getFluid()Lnet/minecraft/world/level/material/Fluid;", remap = true)
    @Expression("?.getType() != ?.getFluid()")
    @ModifyExpressionValue(method = "provideFluidToSpace", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean redirectFluidTypeComparison(
            boolean original,
            @Local(argsOnly = true) FluidStack fluid,
            @Local(name = "fluidState") FluidState fluidState)
    {
        return fluid.getFluid().getFluidType() != fluidState.getFluidType();
    }
}
