package net.apertyotis.createandesiteabound.mixin.create.fluids;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.FlowSource;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.simibubi.create.content.fluids.pipes.StraightPipeBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.apertyotis.createandesiteabound.AllConfig;
import net.apertyotis.createandesiteabound.foundation.FluidTransportBehaviourEx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Optional;

@Mixin(value = FluidTransportBehaviour.class, remap = false)
public abstract class FluidTransportBehaviourMixin extends BlockEntityBehaviour implements FluidTransportBehaviourEx {
    @Unique
    private BlockPos caa$filterPos;

    @Unique
    private boolean caa$attached = false;

    @Unique
    private boolean caa$needUpdate = false;

    @Unique
    private boolean caa$noSource = false;

    public FluidTransportBehaviourMixin(SmartBlockEntity be) {
        super(be);
    }

    @Unique
    @Override
    public void caa$attachFilterPos(BlockPos pos) {
        if (blockEntity instanceof FluidPipeBlockEntity || blockEntity instanceof StraightPipeBlockEntity) {
            if (caa$attached) {
                if (caa$filterPos != null && !caa$filterPos.equals(pos))
                    caa$filterPos = null;
            } else {
                caa$filterPos = pos;
                caa$attached = true;
            }
        }
    }

    @Unique
    @Override
    public BlockPos caa$getFilterPos() {
        return caa$filterPos;
    }

    @Unique
    @Override
    public void caa$pressureChanged() {
        caa$filterPos = null;
        caa$attached = false;
        caa$noSource = false;
    }

    @Unique
    @Override
    public void caa$scheduleUpdate() {
        caa$needUpdate = true;
    }

    @Inject(method = "canPullFluidFrom", at = @At("RETURN"), cancellable = true)
    private void canPullFluidFromWithFilter(
        FluidStack fluid, BlockState state, Direction direction, CallbackInfoReturnable<Boolean> cir
    ) {
        Level level = blockEntity.getLevel();
        if (cir.getReturnValue() && caa$filterPos != null && level != null) {
            if (!level.isLoaded(caa$filterPos)) {
                cir.setReturnValue(false);
                return;
            }
            if (level.getBlockEntity(caa$filterPos) instanceof SmartBlockEntity otherPipe) {
                FluidTransportBehaviour pipeBehaviour = otherPipe.getBehaviour(FluidTransportBehaviour.TYPE);
                if (pipeBehaviour != null)
                    cir.setReturnValue(pipeBehaviour.canPullFluidFrom(fluid, otherPipe.getBlockState(), direction));
            }
        }
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void readFilterPos(CompoundTag nbt, boolean clientPacket, CallbackInfo ci) {
        if (nbt.contains("FilterPos")) {
            caa$filterPos = NbtUtils.readBlockPos(nbt.getCompound("FilterPos"));
        }
        if (nbt.contains("ScheduleUpdate")) {
            caa$needUpdate = nbt.getBoolean("ScheduleUpdate");
        }
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void writeFilterPos(CompoundTag nbt, boolean clientPacket, CallbackInfo ci) {
        if (caa$filterPos != null) {
            nbt.put("FilterPos", NbtUtils.writeBlockPos(caa$filterPos));
        }
        if (caa$needUpdate) {
            nbt.putBoolean("ScheduleUpdate", true);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void updateWhenBorderChunkReload(CallbackInfo ci) {
        if (caa$needUpdate) {
            if (blockEntity.getLevel() == null)
                return;
            FluidPropagator.propagateChangedPipe(
                blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState());
            caa$needUpdate = false;
            ci.cancel();
        }
    }

    @Definition(id = "onServer", local = @Local(type = boolean.class, name = "onServer"))
    @Expression("onServer")
    @Inject(method = "tick", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 1), cancellable = true)
    private void tickConnections(CallbackInfo ci) {
        if (!AllConfig.pump_speed_change)
            return;
        ci.cancel();
        if (caa$noSource)
            return;
        // 只 tick 网络源，液流动画传播逻辑移动到流体网络遍历时完成
        FluidTransportBehaviour behaviour = (FluidTransportBehaviour)(Object) this;
        Level world = behaviour.getWorld();
        if (world == null)
            return;
        Collection<PipeConnection> connections = behaviour.interfaces.values();
        boolean changed = false;
        // 缓存管道是否位于源的信息
        boolean noSource = true;
        for (PipeConnection connection: connections) {
            if (connection.comparePressure() >= 0)
                continue;
            Optional<FlowSource> source = ((PipeConnectionAccessor) connection).getSource();
            if (source.isEmpty() && !connection.determineSource(world, behaviour.getPos())) {
                if (connection.determineSource(world, behaviour.getPos())) {
                    source = ((PipeConnectionAccessor) connection).getSource();
                } else {
                    // 让未加载的管道有机会初始化
                    noSource = false;
                    continue;
                }
            }
            if (source.isPresent() && source.get().isEndpoint()) {
                noSource = false;
                changed |= connection.manageFlows(world, behaviour.getPos(), FluidStack.EMPTY, fluid ->
                    behaviour.canPullFluidFrom(fluid, behaviour.blockEntity.getBlockState(), connection.side));
            }
        }
        caa$noSource = noSource;
        if (changed)
            behaviour.blockEntity.notifyUpdate();
    }

    // 网络变化同时清空液流
    @Inject(method = "wipePressure", at = @At("TAIL"))
    private void onNetworkChange(CallbackInfo ci) {
        for (PipeConnection connection: ((FluidTransportBehaviour)(Object) this).interfaces.values()) {
            ((PipeConnectionAccessor) connection).setFlow(Optional.empty());
        }
        caa$noSource = false;
    }

    @Override
    public void unload() {
        for (PipeConnection connection: ((FluidTransportBehaviour)(Object) this).interfaces.values()) {
            connection.resetNetwork();
        }
    }
}
