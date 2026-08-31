package net.apertyotis.createandesiteabound.mixin.create.fluids;

import com.simibubi.create.content.fluids.FlowSource;
import com.simibubi.create.content.fluids.PipeConnection;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(value = PipeConnection.class, remap = false)
public interface PipeConnectionAccessor {
    @Accessor("source")
    Optional<FlowSource> getSource();

    @Accessor("flow")
    Optional<PipeConnection.Flow> getFlow();

    @Accessor("flow")
    void setFlow(Optional<PipeConnection.Flow> flow);

    @Invoker("tryStartingNewFlow")
    boolean invokeTryStartingNewFlow(boolean inbound, FluidStack fluid);
}
