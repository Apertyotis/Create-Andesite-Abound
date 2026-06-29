package net.apertyotis.createandesiteabound.mixin.create.kinetics.belt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltRenderer;
import net.apertyotis.createandesiteabound.content.belt.BeltBlockEntityEx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BeltRenderer.class, remap = false)
public abstract class BeltRendererMixin {
    @WrapOperation(
        method = "renderSafe(Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;getSpeed()F"
        )
    )
    private float redirectGetSpeed1(BeltBlockEntity instance, Operation<Float> original) {
        return ((BeltBlockEntityEx) instance).caa$getTargetSpeed();
    }

    @WrapOperation(
        method = "renderItems",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;getSpeed()F"
        )
    )
    private float redirectGetSpeed2(BeltBlockEntity instance, Operation<Float> original) {
        return ((BeltBlockEntityEx) instance).caa$getTargetSpeed();
    }
}
