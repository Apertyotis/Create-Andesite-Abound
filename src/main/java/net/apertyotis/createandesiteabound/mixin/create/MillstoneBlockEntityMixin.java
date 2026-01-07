package net.apertyotis.createandesiteabound.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MillstoneBlockEntity.class, remap = false)
public abstract class MillstoneBlockEntityMixin {

    @Shadow
    public int timer;

    @Shadow
    abstract public int getProcessingSpeed();

    // 默认配方时间设为160，即满速需处理10tick，并减少1tick计时抵消配方搜索耗时
    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/kinetics/millstone/MillstoneBlockEntity;timer:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void modifyDefaultTimer(CallbackInfo ci) {
        timer = Math.max(160 - getProcessingSpeed(), 1);
    }

    // 配方时间翻16倍，使满速时为1倍速，并减少1tick计时抵消配方搜索耗时
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/millstone/MillingRecipe;getProcessingDuration()I"
            )
    )
    private int modifyProcessingDuration(MillingRecipe instance, Operation<Integer> original) {
        return Math.max(original.call(instance) * 16 - getProcessingSpeed(), 1);
    }
}
