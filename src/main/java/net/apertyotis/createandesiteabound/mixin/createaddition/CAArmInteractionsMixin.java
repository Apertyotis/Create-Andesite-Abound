package net.apertyotis.createandesiteabound.mixin.createaddition;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrh0.createaddition.index.CAArmInteractions;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CAArmInteractions.class, remap = false)
public abstract class CAArmInteractionsMixin {
    @WrapOperation(
        method = "register(Ljava/lang/String;Ljava/util/function/Function;)Lcom/simibubi/create/content/kinetics/mechanicalArm/ArmInteractionPointType;",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/mechanicalArm/ArmInteractionPointType;register(Lcom/simibubi/create/content/kinetics/mechanicalArm/ArmInteractionPointType;)V"
        )
    )
    private static void removeArmInteractions(ArmInteractionPointType type, Operation<Void> original) {
        // 移除动力臂与带吸管烈焰人的交互，因为在0.5.1版本中该注册方法不安全
    }
}
