package net.apertyotis.createandesiteabound.mixin.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock;
import com.jesz.createdieselgenerators.blocks.PoweredEngineShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = PoweredEngineShaftBlock.class, remap = false)
public abstract class PoweredEngineShaftBlockMixin extends PoweredShaftBlock {

    public PoweredEngineShaftBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        for (Direction d : Iterate.directions) {
            if (d.getAxis() == state.getValue(BlockStateProperties.AXIS))
                continue;
            BlockPos enginePos = pos.relative(d, 2);
            BlockState engineState = level.getBlockState(enginePos);
            if (!(engineState.getBlock() instanceof HugeDieselEngineBlock))
                continue;
            if (!enginePos.relative(engineState.getValue(HugeDieselEngineBlock.FACING), 2).equals(pos))
                continue;
            return true;
        }
        return false;
    }
}
