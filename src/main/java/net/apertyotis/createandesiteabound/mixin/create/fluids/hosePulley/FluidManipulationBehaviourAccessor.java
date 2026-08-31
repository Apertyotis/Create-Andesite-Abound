package net.apertyotis.createandesiteabound.mixin.create.fluids.hosePulley;

import com.simibubi.create.content.fluids.transfer.FluidManipulationBehaviour;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(value = FluidManipulationBehaviour.class, remap = false)
public interface FluidManipulationBehaviourAccessor {
    @Accessor("visited")
    Set<BlockPos> getVisited();
}
