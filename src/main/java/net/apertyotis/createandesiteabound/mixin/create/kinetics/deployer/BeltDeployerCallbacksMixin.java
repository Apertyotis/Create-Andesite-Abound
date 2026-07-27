package net.apertyotis.createandesiteabound.mixin.create.kinetics.deployer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.BeltDeployerCallbacks;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import net.apertyotis.createandesiteabound.AllConfig;
import net.apertyotis.createandesiteabound.mixin.create.kinetics.belt.BeltInventoryAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = BeltDeployerCallbacks.class, remap = false)
public abstract class BeltDeployerCallbacksMixin {
    // 让机械手在传送带和置物台上时不再跳过 WAITING
    @WrapOperation(
        method = "onItemReceived",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity;start()V"
        )
    )
    private static void waitStart_1(DeployerBlockEntity instance, Operation<Void> original) {
        if (!AllConfig.deployer_speed_change) {
            original.call(instance);
            return;
        }

        DeployerBlockEntityAccessor accessor = (DeployerBlockEntityAccessor) instance;
        if (accessor.getTimer() <= -1000) {
            original.call(instance);
        }
    }

    // 让机械手在置物台上时不再跳过 WAITING
    @WrapOperation(
        method = "whenItemHeld",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity;start()V"
        )
    )
    private static void waitStart_2(DeployerBlockEntity instance, Operation<Void> original) {
        if (!AllConfig.deployer_speed_change) {
            original.call(instance);
            return;
        }

        DeployerBlockEntityAccessor accessor = (DeployerBlockEntityAccessor) instance;
        if (accessor.getTimer() <= -1000) {
            original.call(instance);
        }
    }

    // 重写传送带加工产物处理逻辑
    @Inject(
        method = "whenItemHeld",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/deployer/BeltDeployerCallbacks;activate(Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;Lcom/simibubi/create/content/kinetics/belt/behaviour/TransportedItemStackHandlerBehaviour;Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;)V",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private static void removeEmpty(
        TransportedItemStack s, TransportedItemStackHandlerBehaviour i,
        DeployerBlockEntity blockEntity, CallbackInfoReturnable<BeltProcessingBehaviour.ProcessingResult> cir
    ) {
        if (s.stack.isEmpty())
            cir.setReturnValue(BeltProcessingBehaviour.ProcessingResult.REMOVE);
    }

    @WrapOperation(
        method = "activate",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/behaviour/TransportedItemStackHandlerBehaviour;handleProcessingOnItem(Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;Lcom/simibubi/create/content/kinetics/belt/behaviour/TransportedItemStackHandlerBehaviour$TransportedResult;)V"
        )
    )
    private static void fastOutput(
        TransportedItemStackHandlerBehaviour handler, TransportedItemStack transported,
        TransportedItemStackHandlerBehaviour.TransportedResult processOutput, Operation<Void> original
    ) {
        if (!(handler.blockEntity instanceof BeltBlockEntity belt)) {
            original.call(handler, transported, processOutput);
            return;
        }

        BeltBlockEntity beltBE = belt.getControllerBE();
        if (beltBE == null)
            return;
        BeltInventoryAccessor inv = (BeltInventoryAccessor) beltBE.getInventory();
        if (inv == null)
            return;

        if (processOutput.hasHeldOutput()) {
            TransportedItemStack left = processOutput.getHeldOutput();
            // noinspection DataFlowIssue
            if (!left.stack.isEmpty()) {
                left.beltPosition = belt.index + .5f - (inv.isPositive() ? 1 / 512f : -1 / 512f);
                inv.getToInsert().add(left);
            }

            // processOutput.hasHeldOutput() 为真足以保证 output 非空
            // 将第一份产出直接替换已有的物品堆，使得产出能立即输出到当前格漏斗，并节约性能
            List<TransportedItemStack> output = processOutput.getOutputs();
            transported.stack = output.get(0).stack;
            transported.angle = output.get(0).angle;
            transported.locked = false;
            for (int i = 1; i < output.size(); i++) {
                inv.getToInsert().add(output.get(i));
            }
        } else {
            transported.stack.shrink(1);
        }
        beltBE.notifyUpdate();
    }
}
