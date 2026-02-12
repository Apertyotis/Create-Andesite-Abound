package net.apertyotis.createandesiteabound.mixin.create.kinetics;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RotationPropagator.class, remap = false)
public abstract class RotationPropagatorMixin {
    /**
     * 修复*已屏蔽的禁忌知识*导致坏档的问题<br>
     * 详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/9803">#9803</a>
     */
    @WrapOperation(
            method = "propagateNewSource",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;getTheoreticalSpeed()F",
                    ordinal = 2
            )
    )
    private static float redirectRPM(
            KineticBlockEntity instance, Operation<Float> original,
            @Local(name = "newSpeed") float newSpeed)
    {
        float speed = original.call(instance);
        if (Math.abs(speed - newSpeed) <= 1e-5f)
            return newSpeed;
        else
            return speed;
    }
}
