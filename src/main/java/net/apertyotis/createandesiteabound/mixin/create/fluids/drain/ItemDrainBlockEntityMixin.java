package net.apertyotis.createandesiteabound.mixin.create.fluids.drain;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import net.apertyotis.createandesiteabound.Config;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemDrainBlockEntity.class, remap = false)
public abstract class ItemDrainBlockEntityMixin {
    // 让分液配方处理速度翻倍
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/fluids/drain/ItemDrainBlockEntity;processingTicks:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 1
            )
    )
    private void modifyProcessingTicks(ItemDrainBlockEntity instance, int value, Operation<Void> original) {
        if (!Config.item_drain_speed_change)
            original.call(instance, value);
        else
            original.call(instance, value - 1);
    }

    // 设定每次滚动为1/10格
    @Inject(
            method = "itemMovementPerTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void redirectItemMovementPerTick(CallbackInfoReturnable<Float> cir){
        if (!Config.item_drain_speed_change) return;
        cir.setReturnValue(0.1f);
    }

    // 更改分液配方应用的时机
    @ModifyExpressionValue(
            method = "continueProcessing",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/fluids/drain/ItemDrainBlockEntity;processingTicks:I",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            )
    )
    private int continueProcessing_redirectProcessingTicks(int original) {
        if (!Config.item_drain_speed_change)
            return original;
        else
            return original + 1;
    }
}
