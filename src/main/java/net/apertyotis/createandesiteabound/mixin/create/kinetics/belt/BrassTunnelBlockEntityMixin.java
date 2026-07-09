package net.apertyotis.createandesiteabound.mixin.create.kinetics.belt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import net.apertyotis.createandesiteabound.content.belt.BeltBlockEntityEx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BrassTunnelBlockEntity.class, remap = false)
public abstract class BrassTunnelBlockEntityMixin {
    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;getSpeed()F"
        )
    )
    private float redirectGetSpeed1(BeltBlockEntity instance, Operation<Float> original) {
        return ((BeltBlockEntityEx) instance).caa$getTargetSpeed();
    }

    @WrapOperation(
        method = "addValidOutputsOf",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;getSpeed()F"
        )
    )
    private float redirectGetSpeed2(BeltBlockEntity instance, Operation<Float> original) {
        return ((BeltBlockEntityEx) instance).caa$getTargetSpeed();
    }
}
