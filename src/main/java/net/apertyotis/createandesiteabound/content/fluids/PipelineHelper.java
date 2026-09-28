package net.apertyotis.createandesiteabound.content.fluids;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.foundation.utility.BlockFace;
import net.apertyotis.createandesiteabound.mixin.create.fluids.PipeConnectionAccessor;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

public class PipelineHelper {
    /**
     * 动力泵收到后方的更新时，额外重置前方的液流<br>
     * 流体网络重置时，额外重置前方的液流<br>
     * 另外，传递流体网络更新时会擦除附近到动力泵为止的压力，Mixin 后擦除压力会同时清空液流<br>
     * @see FluidPropagator
     * @see net.apertyotis.createandesiteabound.mixin.create.fluids.pumps.PumpBlockEntityMixin
     * @see net.apertyotis.createandesiteabound.mixin.create.fluids.FluidNetworkMixin
     * @see net.apertyotis.createandesiteabound.mixin.create.fluids.PipeConnectionMixin
     */
    public static void resetFrontPipeline(Level level, BlockFace start, FluidStack fluid) {
        if (fluid.isEmpty()) {
            FluidTransportBehaviour firstPipe = FluidPropagator.getPipe(level, start.getPos());
            if (firstPipe == null)
                return;
            PipeConnection firstConnection = firstPipe.interfaces.get(start.getFace());
            if (firstConnection == null)
                return;
            fluid = firstConnection.getProvidedFluid();
            if (fluid.isEmpty())
                return;
        }

        Queue<BlockFace> frontier = new ArrayDeque<>();
        frontier.add(start);
        while (!frontier.isEmpty()) {
            BlockFace current = frontier.poll();

            FluidTransportBehaviour behaviour = FluidPropagator.getPipe(level, current.getPos());
            if (behaviour == null) {
                continue;
            }
            boolean singleSource = true;
            List<PipeConnection> next = new ArrayList<>(5);
            for (PipeConnection connection: behaviour.interfaces.values()) {
                if (connection.side == current.getFace()) {
                    ((PipeConnectionAccessor) connection).setFlow(Optional.empty());
                } else if (connection.hasFlow()) {
                    if (connection.comparePressure() < 0) {
                        if (connection.getProvidedFluid().isFluidEqual(fluid)) {
                            singleSource = false;
                        }
                    } else if (connection.comparePressure() > 0) {
                        next.add(connection);
                    }
                }
            }
            if (!singleSource)
                continue;
            boolean changed = false;
            for (PipeConnection connection: next) {
                Optional<PipeConnection.Flow> flow = ((PipeConnectionAccessor) connection).getFlow();
                if (flow.isPresent() && flow.get().fluid.isFluidEqual(fluid)) {
                    ((PipeConnectionAccessor) connection).setFlow(Optional.empty());
                    BlockFace nextFace = new BlockFace(current.getPos(), connection.side);
                    frontier.add(nextFace.getOpposite());
                    changed = true;
                }
            }
            if (changed)
                behaviour.blockEntity.notifyUpdate();
        }
    }
}
