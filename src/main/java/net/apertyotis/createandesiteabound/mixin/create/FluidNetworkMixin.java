package net.apertyotis.createandesiteabound.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.FluidNetwork;
import net.apertyotis.createandesiteabound.Config;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
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

    /**
     * 修复多流体容器含有 n 格同种流体时，从中抽取会浪费 n 份流体但只取出一份的问题
     * 详见 Create PR <a href ="https://github.com/Creators-of-Create/Create/pull/9137">#9137</a>
     */
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/fluids/capability/IFluidHandler;getTanks()I"
            )
    )
    private int breakLoop(IFluidHandler instance, Operation<Integer> original,
                          @Local(name = "transfer") FluidStack transfer) {
        if (transfer.isEmpty())
            return original.call(instance);
        else
            return Integer.MIN_VALUE;
    }
}
