package net.apertyotis.createandesiteabound.mixin.create.kinetics.belt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltInstance;
import net.apertyotis.createandesiteabound.content.belt.BeltBlockEntityEx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BeltInstance.class, remap = false)
public abstract class BeltInstanceMixin {
    @WrapOperation(
        method = "getScrollSpeed",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;getSpeed()F"
        )
    )
    private float redirectGetSpeed(BeltBlockEntity instance, Operation<Float> original) {
        return ((BeltBlockEntityEx) instance).caa$getTargetSpeed();
    }
}
