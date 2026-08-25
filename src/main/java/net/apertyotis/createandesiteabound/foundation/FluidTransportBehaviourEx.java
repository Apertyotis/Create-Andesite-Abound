package net.apertyotis.createandesiteabound.foundation;

import net.minecraft.core.BlockPos;

public interface FluidTransportBehaviourEx {
    void caa$attachFilterPos(BlockPos pos);
    BlockPos caa$getFilterPos();
    void caa$resetFilterPos();
    void caa$scheduleUpdate();
}
