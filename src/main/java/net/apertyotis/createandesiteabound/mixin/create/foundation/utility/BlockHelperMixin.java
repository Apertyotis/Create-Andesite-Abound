package net.apertyotis.createandesiteabound.mixin.create.foundation.utility;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = BlockHelper.class, remap = false)
public abstract class BlockHelperMixin {
    @WrapMethod(method = "hasBlockSolidSide")
    private static boolean solidFluidTank(BlockState state, BlockGetter level, BlockPos pos, Direction side, Operation<Boolean> original) {
        if (AllBlocks.FLUID_TANK.has(state))
            return true;
        return original.call(state, level, pos, side);
    }
}
