package net.apertyotis.createandesiteabound.mixin.create.fluids.hosePulley;

import com.simibubi.create.content.fluids.transfer.FluidManipulationBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(value = FluidManipulationBehaviour.class, remap = false)
public interface FluidManipulationBehaviourAccessor {
    @Accessor("visited")
    Set<BlockPos> getVisited();

    @Invoker("canDrainInfinitely")
    boolean invokeCanDrainInfinitely(Fluid fluid);
}
