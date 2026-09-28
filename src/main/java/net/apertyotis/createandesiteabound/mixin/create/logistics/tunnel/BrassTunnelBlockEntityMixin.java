package net.apertyotis.createandesiteabound.mixin.create.logistics.tunnel;

import com.simibubi.create.content.logistics.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.funnel.FunnelBlock;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlock;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.simibubi.create.foundation.utility.Iterate;
import net.apertyotis.createandesiteabound.content.tunnel.BrassTunnelBlockEntityEx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(value = BrassTunnelBlockEntity.class, remap = false)
public abstract class BrassTunnelBlockEntityMixin implements BrassTunnelBlockEntityEx {
    @Shadow
    private Set<BrassTunnelBlockEntity> syncSet;
    @Shadow
    float distributionProgress;
    @Unique
    private boolean caa$sideAttemptExist = false;
    @Unique
    private boolean caa$backSuppression = false;

    @Unique
    @Override
    public void caa$tryInputFromSide() {
        caa$sideAttemptExist = true;
    }

    @Override
    public boolean caa$canInputFromBack() {
        return !caa$backSuppression;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (distributionProgress == -1) {
            caa$backSuppression = false;
        }
    }

    @Inject(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/logistics/tunnel/BrassTunnelBlockEntity;gatherValidOutputs()Ljava/util/List;",
            shift = At.Shift.AFTER
        )
    )
    private void isSingleTunnel(CallbackInfo ci) {
        if (syncSet.size() == 1 && caa$sideAttemptExist) {
            caa$backSuppression = true;
        }
        caa$sideAttemptExist = false;
    }

    @Inject(method = "write", at = @At("HEAD"))
    private void writeEx(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        compound.putBoolean("SideAttempt", caa$sideAttemptExist);
        compound.putBoolean("BackSuppression", caa$backSuppression);
    }

    @Inject(method = "read", at = @At("HEAD"))
    private void readEx(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        caa$sideAttemptExist = compound.getBoolean("SideAttempt");
        caa$backSuppression = compound.getBoolean("BackSuppression");
    }

    @Inject(method = "hasDistributionBehaviour", at = @At("TAIL"), cancellable = true)
    private void hasDistributionBehaviourEx(CallbackInfoReturnable<Boolean> cir) {
        BlockEntity self = (BlockEntity)(Object) this;
        Level level = self.getLevel();
        if (level == null)
            return;
        Axis axis = self.getBlockState().getValue(BrassTunnelBlock.HORIZONTAL_AXIS);
        BlockPos pos = self.getBlockPos();
        for (Direction side: Iterate.directions) {
            if (side.getAxis() == axis || side == Direction.DOWN)
                continue;
            BlockState state = level.getBlockState(pos.relative(side));
            if (FunnelBlock.getFunnelFacing(state) != side)
                continue;
            if (state.getBlock() instanceof FunnelBlock) {
                if (!state.getValue(FunnelBlock.EXTRACTING)) {
                    cir.setReturnValue(true);
                    return;
                }
            } else if (state.getBlock() instanceof BeltFunnelBlock) {
                if (state.getValue(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.PULLING) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }
}
