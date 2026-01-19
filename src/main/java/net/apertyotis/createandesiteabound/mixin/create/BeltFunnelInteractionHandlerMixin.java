package net.apertyotis.createandesiteabound.mixin.create;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.transport.BeltFunnelInteractionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BeltFunnelInteractionHandler.class, remap = false)
public abstract class BeltFunnelInteractionHandlerMixin {
    // 修复传送带上对向漏斗向已满保险库输入物品时不会阻挡传送带的 bug
    @ModifyExpressionValue(
            method = "checkForFunnels",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/inventory/VersionedInventoryTrackerBehaviour;stillWaiting(Lcom/simibubi/create/foundation/blockEntity/behaviour/inventory/InvManipulationBehaviour;)Z"
            ),
            // 0.5.1e及之前没有这个bug，也没有对应的注入点
            require = 0
    )
    private static boolean correctlyBlockItem(boolean original, @Local(name = "blocking") boolean blocking, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        if (original && blocking) {
            cir.setReturnValue(true);
        }
        return original;
    }
}
