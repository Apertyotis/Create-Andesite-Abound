package net.apertyotis.createandesiteabound.mixin.create.kinetics.base;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.apertyotis.createandesiteabound.content.belt.BeltBlockEntityEx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GeneratingKineticBlockEntity.class, remap = false)
public abstract class GeneratingKineticBlockEntityMixin {
    @WrapOperation(
        method = "setSource",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;getSpeed()F"
        )
    )
    private float getBeltKineticSpeed(KineticBlockEntity instance, Operation<Float> original) {
        if (instance instanceof BeltBlockEntityEx ex)
            return ex.caa$getKineticSpeed();
        else
            return original.call(instance);
    }
}
