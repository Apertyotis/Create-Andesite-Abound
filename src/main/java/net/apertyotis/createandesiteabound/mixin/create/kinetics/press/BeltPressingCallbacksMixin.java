package net.apertyotis.createandesiteabound.mixin.create.kinetics.press;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.press.BeltPressingCallbacks;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import net.apertyotis.createandesiteabound.mixin.create.kinetics.belt.BeltInventoryAccessor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = BeltPressingCallbacks.class, remap = false)
public abstract class BeltPressingCallbacksMixin {
    // 优化辊压机传送带加工，并使辊压机像机械手一样在加工完成瞬间即可放行产物
    @WrapOperation(
        method = "whenItemHeld",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/behaviour/TransportedItemStackHandlerBehaviour;handleProcessingOnItem(Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;Lcom/simibubi/create/content/kinetics/belt/behaviour/TransportedItemStackHandlerBehaviour$TransportedResult;)V"
        )
    )
    private static void fastOutput(
        TransportedItemStackHandlerBehaviour handler, TransportedItemStack transported,
        TransportedItemStackHandlerBehaviour.TransportedResult processOutput, Operation<Void> original,
        @Local(name = "bulk") boolean bulk,
        @Cancellable CallbackInfoReturnable<BeltProcessingBehaviour.ProcessingResult> cir
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

        if (bulk) {
            if (processOutput == TransportedItemStackHandlerBehaviour.TransportedResult.removeItem()) {
                inv.getToRemove().add(transported);
            } else {
                List<TransportedItemStack> output = processOutput.getOutputs();
                transported.stack = output.get(0).stack;
                transported.angle = output.get(0).angle;
                transported.locked = false;
                for (int i = 1; i < output.size(); i++) {
                    inv.getToInsert().add(output.get(i));
                }
            }
        } else {
            if (processOutput.hasHeldOutput()) {
                TransportedItemStack left = processOutput.getHeldOutput();
                // noinspection DataFlowIssue
                if (!left.stack.isEmpty()) {
                    left.beltPosition = belt.index + .5f - (inv.isPositive() ? 1 / 512f : -1 / 512f);
                    inv.getToInsert().add(left);
                }

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
        }
        beltBE.notifyUpdate();
        if (transported.stack.isEmpty())
            cir.setReturnValue(BeltProcessingBehaviour.ProcessingResult.REMOVE);
        else
            cir.setReturnValue(BeltProcessingBehaviour.ProcessingResult.HOLD);
    }

    // 修复提前放行产物导致后续原料可能也被放行的问题
    @WrapOperation(
        method = "whenItemHeld",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/content/kinetics/press/PressingBehaviour;running:Z",
            opcode = Opcodes.GETFIELD
        )
    )
    private static boolean startProcessing(
        PressingBehaviour behaviour, Operation<Boolean> original,
        @Local(argsOnly = true) TransportedItemStack transported
    ) {
        boolean running = original.call(behaviour);
        if (!running && behaviour.specifics.tryProcessOnBelt(transported, null, true)) {
            behaviour.start(PressingBehaviour.Mode.BELT);
            running = true;
        }
        return running;
    }
}
