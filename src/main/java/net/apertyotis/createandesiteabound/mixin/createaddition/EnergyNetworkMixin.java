package net.apertyotis.createandesiteabound.mixin.createaddition;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrh0.createaddition.energy.network.EnergyNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EnergyNetwork.class, remap = false)
public abstract class EnergyNetworkMixin {
    @WrapOperation(
        method = "getMaxBuff",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;min(II)I"
        )
    )
    private int unlimitedEnergyNetwork(int a, int b, Operation<Integer> original) {
        return a;
    }
}
