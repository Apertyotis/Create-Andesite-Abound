package net.apertyotis.createandesiteabound.mixin.create.kinetics.deployer;

import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = DeployerBlockEntity.class, remap = false)
public interface DeployerBlockEntityAccessor {
    @Accessor("timer")
    int getTimer();
}
