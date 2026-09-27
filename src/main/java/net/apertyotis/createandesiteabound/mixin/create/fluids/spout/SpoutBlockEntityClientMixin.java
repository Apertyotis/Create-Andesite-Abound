package net.apertyotis.createandesiteabound.mixin.create.fluids.spout;

import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SpoutBlockEntity.class, remap = false)
public abstract class SpoutBlockEntityClientMixin {
    @Inject(method = "spawnSplash", at = @At("HEAD"), cancellable = true)
    private void spawnNonemptySplash(FluidStack fluid, CallbackInfo ci) {
        if (fluid == null || fluid.isEmpty())
            ci.cancel();
    }

    @Inject(method = "spawnProcessingParticles", at = @At("HEAD"), cancellable = true)
    private void spawnNonemptyParticles(FluidStack fluid, CallbackInfo ci) {
        if (fluid == null || fluid.isEmpty())
            ci.cancel();
    }
}
