package net.apertyotis.createandesiteabound.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.deployer.BeltDeployerCallbacks;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import net.apertyotis.createandesiteabound.mixin.accessor.DeployerBlockEntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BeltDeployerCallbacks.class, remap = false)
public abstract class BeltDeployerCallbacksMixin {
    @WrapOperation(
            method = "whenItemHeld",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity;start()V"
            )
    )
    private static void waitStart(DeployerBlockEntity instance, Operation<Void> original) {
        // 让机械手在传送带上时不再跳过WAIT
        DeployerBlockEntityAccessor accessor = (DeployerBlockEntityAccessor) instance;
        if (accessor.getTimer() <= -1000) {
            original.call(instance);
        }
    }
}
