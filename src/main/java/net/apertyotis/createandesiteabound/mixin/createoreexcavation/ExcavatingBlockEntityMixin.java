package net.apertyotis.createandesiteabound.mixin.createoreexcavation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.tom.createores.block.entity.ExcavatingBlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ExcavatingBlockEntity.class, remap = false)
public abstract class ExcavatingBlockEntityMixin {
    @Shadow
    protected int progress;

    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/base/IRotate$SpeedLevel;getSpeedValue()F",
            ordinal = 0
        )
    )
    private float fixedSpeedValue1(IRotate.SpeedLevel instance, Operation<Float> original) {
        return 256;
    }

    @WrapOperation(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lcom/tom/createores/block/entity/ExcavatingBlockEntity;progress:I",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0
        )
    )
    private void fixedProgress(ExcavatingBlockEntity<?> instance, int value, Operation<Void> original) {
        original.call(instance, progress + 1);
    }

    @WrapOperation(
        method = "write",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/base/IRotate$SpeedLevel;getSpeedValue()F"
        )
    )
    private float fixedSpeedValue2(IRotate.SpeedLevel instance, Operation<Float> original) {
        return 256;
    }
}
