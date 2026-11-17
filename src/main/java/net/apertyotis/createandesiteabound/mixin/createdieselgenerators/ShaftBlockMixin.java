package net.apertyotis.createandesiteabound.mixin.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock;
import com.jesz.createdieselgenerators.blocks.PoweredEngineShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ShaftBlock.class, remap = false)
public abstract class ShaftBlockMixin {
    @Inject(method = "pickCorrectShaftType", at = @At("HEAD"), cancellable = true)
    private static void pickCorrectShaftType(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        for (Direction d : Iterate.directions) {
            if (d.getAxis() == state.getValue(BlockStateProperties.AXIS))
                continue;
            BlockPos enginePos = pos.relative(d, 2);
            BlockState engineState = level.getBlockState(enginePos);
            if (!(engineState.getBlock() instanceof HugeDieselEngineBlock))
                continue;
            if (!enginePos.relative(engineState.getValue(HugeDieselEngineBlock.FACING), 2).equals(pos))
                continue;
            cir.setReturnValue(PoweredEngineShaftBlock.getEquivalent(state));
            break;
        }
    }
}
