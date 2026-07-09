package net.apertyotis.createandesiteabound.foundation;

import net.minecraft.core.BlockPos;

public interface FluidTransportBehaviourEx {
    void caa$attachFilterPos(BlockPos pos);
    void caa$resetFilterPos();
    void caa$scheduleUpdate();
}
