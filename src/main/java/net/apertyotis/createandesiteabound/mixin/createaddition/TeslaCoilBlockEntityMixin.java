package net.apertyotis.createandesiteabound.mixin.createaddition;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mrh0.createaddition.blocks.tesla_coil.TeslaCoilBlockEntity;
import com.mrh0.createaddition.energy.InternalEnergyStorage;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = TeslaCoilBlockEntity.class, remap = false)
public abstract class TeslaCoilBlockEntityMixin {
    @Shadow
    private int chargeAccumulator;

    @WrapOperation(
        method = "chargeRecipe",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mrh0/createaddition/energy/InternalEnergyStorage;internalConsumeEnergy(I)I"
        )
    )
    private int speedUpOnBelt(
        InternalEnergyStorage localEnergy, int toConsume, Operation<Integer> original,
        @Local(argsOnly = true) TransportedItemStackHandlerBehaviour handler
    ) {
        // 处理传送带上物品时，第 1tick 完成 3tick 的进度，以抵消传送带的 2gt 延迟
        if (chargeAccumulator == 0 && handler.blockEntity instanceof BeltBlockEntity) {
            toConsume *= 3;
        }
        return original.call(localEnergy, toConsume);
    }
}
