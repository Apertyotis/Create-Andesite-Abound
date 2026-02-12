package net.apertyotis.createandesiteabound.mixin.create.kinetics.deployer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.deployer.BeltDeployerCallbacks;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import net.apertyotis.createandesiteabound.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
        if (!Config.deployer_speed_change) {
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
        if (!Config.deployer_speed_change) {
            original.call(instance);
            return;
        }

        DeployerBlockEntityAccessor accessor = (DeployerBlockEntityAccessor) instance;
        if (accessor.getTimer() <= -1000) {
            original.call(instance);
        }
    }
}
