package net.apertyotis.createandesiteabound.mixin.create.fluids;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.*;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import net.apertyotis.createandesiteabound.AllConfig;
import net.apertyotis.createandesiteabound.foundation.FluidNetworkEx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(value = PipeConnection.class, remap = false)
public abstract class PipeConnectionMixin {
    @Shadow
    Optional<PipeConnection.Flow> flow;

    @Shadow
    Optional<FlowSource> source;

    @Shadow
    Optional<FlowSource> previousSource;

    @Shadow
    public abstract boolean determineSource(Level world, BlockPos pos);

    @Shadow
    Optional<FluidNetwork> network;

    @Shadow
    protected abstract boolean tryStartingNewFlow(boolean inbound, FluidStack providedFluid);

    // 增强流体网络的容错能力
    @Inject(method = "manageSource", at = @At("HEAD"))
    private void validateSource(Level world, BlockPos pos, CallbackInfo ci) {
        if (source.isPresent() && world.getGameTime() % 20 == 0) {
            Direction side = ((PipeConnection)(Object) this).side;
            BlockPos relative = pos.relative(side);
            if (world.getChunk(relative.getX() >> 4, relative.getZ() >> 4, ChunkStatus.FULL, false) == null)
                return;
            FlowSource flowSource = source.get();
            // 认为开口管道、连接到外部储罐的管道均为不可信任的
            if (flowSource instanceof OpenEndedPipe) {
                if (!FluidPropagator.isOpenEnd(world, pos, side)) {
                    previousSource = source;
                    source = Optional.empty();
                }
            } else if (flowSource instanceof FlowSource.FluidHandler) {
                if (!FluidPropagator.hasFluidCapability(world, relative, side.getOpposite()) ||
                    !flowSource.provideHandler().isPresent()
                ) {
                    source = Optional.empty();
                }
            }
        }
    }

    @Inject(method = "manageFlows", at = @At("HEAD"), cancellable = true)
    private void betterManageFlows(
        Level world, BlockPos pos, FluidStack ignored,
        Predicate<FluidStack> filter, CallbackInfoReturnable<Boolean> cir
    ) {
        if (!AllConfig.pump_speed_change)
            return;
        PipeConnection connection = (PipeConnection)(Object) this;

        // 仅区块卸载时会无效化原流体网络
        if (source.isEmpty() && !determineSource(world, pos)) {
            network = Optional.empty();
            cir.setReturnValue(false);
            return;
        }

        // 源液体流变化不再抛弃原流体网络，需要流体网络批量处理液流
        boolean changed = false;
        FluidStack oldFluid = FluidStack.EMPTY;
        FlowSource flowSource = source.get();
        if (!connection.hasFlow()) {
            changed = tryStartingNewFlow(true, flowSource.provideFluid(filter));
        } else {
            PipeConnection.Flow flow = this.flow.get();
            IFluidHandler handler = flowSource.provideHandler().resolve().orElse(null);
            FluidStack extracted = handler == null ? FluidStack.EMPTY :
                handler.drain(flow.fluid, IFluidHandler.FluidAction.SIMULATE);
            if (extracted.isEmpty() || !filter.test(extracted)) {
                oldFluid = flow.fluid;
                this.flow = Optional.empty();
                changed = true;
            }
        }

        if (network.isEmpty()) {
            // 不再传入 flowSource::provideHandler，flowSource 可能来自已失效的旧 source
            network = Optional.of(new FluidNetwork(world, new BlockFace(pos, connection.side), () -> {
                if (source.isPresent()) {
                    return source.get().provideHandler();
                } else {
                    return LazyOptional.empty();
                }
            }));
            if (network.get() instanceof FluidNetworkEx ex)
                ex.caa$setOldFluid(oldFluid);
        }
        network.get().tick();

        cir.setReturnValue(changed);
    }

    // 动画速度覆盖
    @WrapOperation(
        method = "tickFlowProgress",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/foundation/utility/animation/LerpedFloat;setValue(D)V"
        )
    )
    private void immediateFlow(LerpedFloat instance, double value, Operation<Void> original) {
        if (AllConfig.pump_speed_change)
            value = 1;
        original.call(instance, value);
    }
}
