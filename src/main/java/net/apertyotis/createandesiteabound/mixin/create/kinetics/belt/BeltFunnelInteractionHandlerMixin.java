package net.apertyotis.createandesiteabound.mixin.create.kinetics.belt;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.transport.BeltFunnelInteractionHandler;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import net.apertyotis.createandesiteabound.Config;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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
    private static boolean correctlyBlockItem(
            boolean original,
            @Local(name = "blocking") boolean blocking,
            @Cancellable CallbackInfoReturnable<Boolean> cir)
    {
        if (original && blocking) {
            cir.setReturnValue(true);
        }
        return original;
    }

    // 修复传送带加工产物会被漏斗推回中点，导致增殖类配方无限执行的问题
    // 现在略微跃过漏斗阻挡判定点的物品不会被推回
    @WrapOperation(
            method = "checkForFunnels",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;beltPosition:F",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private static void preventPushItem(
            TransportedItemStack instance,
            float value,
            Operation<Void> original,
            @Local(name = "beltMovementPositive") boolean beltMovementPositive)
    {
        if ((beltMovementPositive && instance.beltPosition < value) ||
                (!beltMovementPositive && instance.beltPosition > value))
        {
            // 未到达判定点的物品会正常被吸附
            original.call(instance, value);
        } else if (Math.abs(instance.beltPosition - value) > 0.1) {
            // 已跃过判定点的物品，只有超出距离较大时才会被推回
            original.call(instance, value);
        }
    }

    // 更改传送带上漏斗判定点位置，从开区间改为闭区间
    // 这样被传送带上对向漏斗阻挡的物品堆刚好能被侧面漏斗吸取
    @ModifyVariable(
            method = "checkForFunnels",
            at = @At("STORE"),
            name = "hasCrossed"
    )
    private static boolean redirectHasCrossed(
            boolean value,
            @Local(argsOnly = true) float nextOffset,
            @Local(name = "beltMovementPositive") boolean beltMovementPositive,
            @Local(name = "funnelEntry") float funnelEntry)
    {
        if (!Config.belt_funnel_detection_tweak) return value;

        return (nextOffset >= funnelEntry && beltMovementPositive) ||
                (nextOffset <= funnelEntry && !beltMovementPositive);
    }
}
