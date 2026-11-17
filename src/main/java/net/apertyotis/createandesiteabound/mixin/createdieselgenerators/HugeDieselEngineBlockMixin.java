package net.apertyotis.createandesiteabound.mixin.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = HugeDieselEngineBlock.class, remap = false)
public abstract class HugeDieselEngineBlockMixin extends Block {

    public HugeDieselEngineBlockMixin(Properties pProperties) {
        super(pProperties);
    }

    @Inject(method = "onWrenched", at = @At("HEAD"), cancellable = true)
    private void ignoredWrench(BlockState state, UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        cir.setReturnValue(InteractionResult.PASS);
    }

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true, remap = true)
    private void ignoreWater(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = defaultBlockState();
        if (context.getPlayer() != null) {
            Direction facing = context.getNearestLookingDirection();
            if (!context.getPlayer().isShiftKeyDown())
                facing = facing.getOpposite();
            state = state.setValue(BlockStateProperties.FACING, facing);
        }
        cir.setReturnValue(state);
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true, remap = true)
    private void ignoreSide(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moving, CallbackInfo ci) {
        super.neighborChanged(state, level, pos, block, otherPos, moving);
        boolean powered = level.hasNeighborSignal(pos);
        if (state.getValue(BlockStateProperties.POWERED) != powered)
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, powered));
        ci.cancel();
    }
}
