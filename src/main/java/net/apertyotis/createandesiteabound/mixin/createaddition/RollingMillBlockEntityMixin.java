package net.apertyotis.createandesiteabound.mixin.createaddition;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.mrh0.createaddition.blocks.rolling_mill.RollingMillBlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RollingMillBlockEntity.class, remap = false)
public abstract class RollingMillBlockEntityMixin {

    @Shadow
    protected abstract void process();

    @Definition(id = "timer", field = "Lcom/mrh0/createaddition/blocks/rolling_mill/RollingMillBlockEntity;timer:I")
    @Expression("?.timer > 0")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean immediatelyProcessing(boolean original, @Cancellable CallbackInfo ci) {
        RollingMillBlockEntity self = (RollingMillBlockEntity)(Object) this;
        // noinspection DataFlowIssue
        if (self.getLevel().isClientSide)
            return original;

        self.timer -= self.getProcessingSpeed();
        if (self.timer <= 0)
            process();
        else
            ci.cancel();
        return false;
    }

    @WrapOperation(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lcom/mrh0/createaddition/blocks/rolling_mill/RollingMillBlockEntity;timer:I",
            opcode = Opcodes.PUTFIELD
        )
    )
    private void defaultDuration(RollingMillBlockEntity instance, int value, Operation<Void> original) {
        original.call(instance, 100);
    }

    @Inject(method = "getProcessingDuration", at = @At("RETURN"), cancellable = true)
    private static void scaledDuration(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() * 16);
    }
}
