package net.apertyotis.createandesiteabound.mixin.create.foundation.utility;

import com.simibubi.create.foundation.utility.BlockFace;
import net.apertyotis.createandesiteabound.foundation.BlockFaceEx;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = BlockFace.class, remap = false)
public abstract class BlockFaceMixin implements BlockFaceEx {
    @Unique
    private BlockPos caa$filterPos = null;

    @Unique
    @Override
    public BlockPos caa$getFilterPos() {
        return caa$filterPos;
    }

    @Unique
    @Override
    public void caa$withFilterPos(BlockPos pos) {
        caa$filterPos = pos;
    }
}
