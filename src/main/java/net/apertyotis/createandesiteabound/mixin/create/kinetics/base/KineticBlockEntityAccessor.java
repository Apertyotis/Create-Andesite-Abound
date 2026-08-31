package net.apertyotis.createandesiteabound.mixin.create.kinetics.base;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = KineticBlockEntity.class, remap = false)
public interface KineticBlockEntityAccessor {
    @Accessor("networkSize")
    void setNetworkSize(int value);

    @Accessor("flickerTally")
    void setFlickerTally(int value);
}
