package net.apertyotis.createandesiteabound.mixin.design_decor;

import com.mangomilk.design_decor.blocks.large_boiler.aluminum.AluminumLargeBoilerBlock;
import com.mangomilk.design_decor.blocks.large_boiler.andesite.AndesiteLargeBoilerBlock;
import com.mangomilk.design_decor.blocks.large_boiler.brass.BrassLargeBoilerBlock;
import com.mangomilk.design_decor.blocks.large_boiler.capitalism.CapitalismLargeBoilerBlock;
import com.mangomilk.design_decor.blocks.large_boiler.cast_iron.CastIronLargeBoilerBlock;
import com.mangomilk.design_decor.blocks.large_boiler.copper.CopperLargeBoilerBlock;
import com.mangomilk.design_decor.blocks.large_boiler.gold.GoldLargeBoilerBlock;
import com.mangomilk.design_decor.blocks.large_boiler.industrial_iron.IndustrialIronLargeBoilerBlock;
import com.mangomilk.design_decor.blocks.large_boiler.zinc.ZincLargeBoilerBlock;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {
        AluminumLargeBoilerBlock.class,
        AndesiteLargeBoilerBlock.class,
        BrassLargeBoilerBlock.class,
        CapitalismLargeBoilerBlock.class,
        CastIronLargeBoilerBlock.class,
        CopperLargeBoilerBlock.class,
        GoldLargeBoilerBlock.class,
        IndustrialIronLargeBoilerBlock.class,
        ZincLargeBoilerBlock.class
}, remap = false)
public class LargeBoilerMixin {
    // 更宽松的检查逻辑
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = true)
    private void simpleCheck(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom, CallbackInfo ci) {
        Direction.Axis axis = pState.getValue(BlockStateProperties.FACING).getAxis();
        boolean ok = true;
        Outer: for (Direction side: Iterate.directions) {
            if (side.getAxis() == axis)
                continue;
            for(boolean secondary : Iterate.falseAndTrue) {
                Direction targetSide = secondary ? side.getClockWise(axis) : side;
                BlockPos structurePos = (secondary ? pPos.relative(side) : pPos).relative(targetSide);
                BlockState occupiedState = pLevel.getBlockState(structurePos);
                if (!occupiedState.hasProperty(BlockStateProperties.FACING)) {
                    if (occupiedState.canBeReplaced())
                        return;
                    ok = false;
                    break Outer;
                }

                BlockPos pos1 = structurePos.relative(occupiedState.getValue(BlockStateProperties.FACING));
                if (secondary) {
                    BlockState occupiedState2 = pLevel.getBlockState(pos1);
                    if (occupiedState2.hasProperty(BlockStateProperties.FACING)) {
                        BlockPos pos2 = pos1.relative(occupiedState2.getValue(BlockStateProperties.FACING));
                        if (pos2.equals(pPos))
                            continue;
                    } else if (occupiedState2.canBeReplaced()) {
                        return;
                    }
                } else if (pos1.equals(pPos)) {
                    continue;
                }
                ok = false;
                break Outer;
            }
        }
        if (ok)
            ci.cancel();
    }
}
