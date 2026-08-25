package net.apertyotis.createandesiteabound.mixin.create.fluids;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.FluidNetwork;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FluidNetwork.class, remap = false)
public abstract class FluidNetworkMixin {
    @Shadow
    FluidStack fluid;

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
        if (!AllConfig.pump_speed_change)
            original.call(instance, value);
        else
            original.call(instance, value * 8);
    }

    /**
     * 修复多流体容器含有 n 格同种流体时，从中抽取会浪费 n 份流体但只取出一份的问题<br>
     * 详见 Create PR <a href ="https://github.com/Creators-of-Create/Create/pull/9137">#9137</a>
     */
    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fluids/capability/IFluidHandler;getTanks()I"
        )
    )
    private int breakFindFluidLoop(
        IFluidHandler instance, Operation<Integer> original,
        @Local(name = "transfer") FluidStack transfer
    ) {
        if (transfer.isEmpty())
            return original.call(instance);
        else
            return 0;
    }

    /**
     * 阻止管道抽取 0mB 液体<br>
     * 详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/10055">#10055</a>
     */
    @Definition(id = "flowSpeed", local = @Local(type = int.class, name = "flowSpeed"))
    @Expression("flowSpeed - ?")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int preventDrainZero(int original, @Cancellable CallbackInfo ci) {
        if (original <= 0)
            ci.cancel();
        return original;
    }

    @WrapOperation(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraftforge/fluids/FluidStack;EMPTY:Lnet/minecraftforge/fluids/FluidStack;",
            opcode = Opcodes.GETSTATIC,
            ordinal = 0
        )
    )
    private FluidStack immediatelyDrain(
        Operation<FluidStack> original,
        @Local(name = "flowSpeed") int flowSpeed,
        @Local(name = "action") IFluidHandler.FluidAction action,
        @Local(name = "handler") IFluidHandler handler,
        @Cancellable CallbackInfo ci
    ) {
        FluidStack toExtract = fluid.copy();
        if (toExtract.isEmpty()) {
            ci.cancel();
            return FluidStack.EMPTY;
        }
        toExtract.setAmount(flowSpeed);
        FluidStack extracted = handler.drain(toExtract, action);
        if (extracted.isEmpty())
            ci.cancel();
        return extracted;
    }
}
