package net.apertyotis.createandesiteabound.mixin.create.kinetics.millstone;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import net.apertyotis.createandesiteabound.Config;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = MillstoneBlockEntity.class, remap = false)
public abstract class MillstoneBlockEntityMixin {

    @Shadow
    abstract public int getProcessingSpeed();

    // 默认配方时间设为160，即满速需处理10tick，并减少1tick计时抵消配方搜索耗时
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/kinetics/millstone/MillstoneBlockEntity;timer:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 1
            )
    )
    private void modifyDefaultTimer(MillstoneBlockEntity instance, int value, Operation<Void> original) {
        if (!Config.millstone_speed_change)
            original.call(instance, value);
        else
            original.call(instance, Math.max(160 - getProcessingSpeed(), 1));
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
        if (!Config.millstone_speed_change)
            return original.call(instance);
        else
            return Math.max(original.call(instance) * 16 - getProcessingSpeed(), 1);
    }
}
