package net.apertyotis.createandesiteabound.mixin.create.processing.burner;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlazeBurnerBlockEntity.class, remap = false)
public abstract class BlazeBurnerBlockEntityMixin {
    @Shadow
    protected BlazeBurnerBlockEntity.FuelType activeFuel;

    @Shadow
    protected int remainingBurnTime;

    @Shadow
    protected boolean isCreative;

    @Shadow
    protected abstract void playSound();

    @Shadow
    protected abstract void setBlockHeat(BlazeBurnerBlock.HeatLevel heat);

    @Inject(method = "applyCreativeFuel", at = @At("HEAD"), cancellable = true)
    private void redirectSetBlockHeat(CallbackInfo ci) {
        if (!isCreative) {
            ci.cancel();
            activeFuel = BlazeBurnerBlockEntity.FuelType.NONE;
            remainingBurnTime = 0;
            isCreative = true;
            playSound();
            setBlockHeat(BlazeBurnerBlock.HeatLevel.KINDLED);
        }
    }
}
