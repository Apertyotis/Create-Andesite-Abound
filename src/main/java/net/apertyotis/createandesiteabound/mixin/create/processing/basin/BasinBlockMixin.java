package net.apertyotis.createandesiteabound.mixin.create.processing.basin;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.logistics.funnel.FunnelBlock;
import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BasinBlock.class, remap = false)
public abstract class BasinBlockMixin {

    @Inject(method = "canOutputTo", at = @At("HEAD"), cancellable = true)
    private static void faucetIgnoreBlock(BlockGetter world, BlockPos basinPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        BlockPos neighbour = basinPos.relative(direction);
        BlockPos output = neighbour.below();
        BlockState blockState = world.getBlockState(neighbour);

        if (FunnelBlock.isFunnel(blockState)) {
            if (FunnelBlock.getFunnelFacing(blockState) == direction) {
                cir.setReturnValue(false);
                return;
            }
        }

        DirectBeltInputBehaviour behaviour = BlockEntityBehaviour.get(world, output, DirectBeltInputBehaviour.TYPE);
        if (behaviour != null) {
            if (behaviour.blockEntity instanceof BeltBlockEntity belt) {
                cir.setReturnValue(belt.getSpeed() == 0 || belt.getMovementFacing() != direction.getOpposite());
            } else {
                cir.setReturnValue(behaviour.canInsertFromSide(direction));
            }
        } else {
            cir.setReturnValue(false);
        }
    }
}
