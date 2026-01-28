package net.apertyotis.createandesiteabound.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.FluidNetwork;
import net.apertyotis.createandesiteabound.Config;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FluidNetwork.class, remap = false)
public abstract class FluidNetworkMixin {
    // 将流体网络传输速度乘8
    @WrapOperation(
            method = "tick",
            at = @At(
                  value = "FIELD",
                  target = "Lcom/simibubi/create/content/fluids/FluidNetwork;transferSpeed:I",
                  opcode = Opcodes.PUTFIELD
            )
    )
    private void redirectTransferSpeed(FluidNetwork instance, int value, Operation<Void> original) {
        if (!Config.pump_speed_change)
            original.call(instance, value);
        else
            original.call(instance, value * 8);
    }
}
