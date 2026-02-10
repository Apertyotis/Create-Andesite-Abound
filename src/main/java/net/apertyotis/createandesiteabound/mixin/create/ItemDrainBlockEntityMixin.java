package net.apertyotis.createandesiteabound.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import net.apertyotis.createandesiteabound.Config;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemDrainBlockEntity.class, remap = false)
public abstract class ItemDrainBlockEntityMixin {
    @Shadow
    protected int processingTicks;

    // 让分液配方处理速度翻倍
    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/fluids/drain/ItemDrainBlockEntity;processingTicks:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void modifyProcessingTicks(CallbackInfo ci) {
        if (!Config.item_drain_speed_change) return;
        processingTicks--;
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
    @WrapOperation(
            method = "continueProcessing",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/fluids/drain/ItemDrainBlockEntity;processingTicks:I",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            )
    )
    private int redirectProcessingTicks_1(ItemDrainBlockEntity instance, Operation<Integer> original) {
        if (!Config.item_drain_speed_change) return original.call(instance);
        return original.call(instance) + 1;
    }
}
