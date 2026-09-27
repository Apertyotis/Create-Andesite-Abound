package net.apertyotis.createandesiteabound.mixin.create.redstone.smartObserver;

import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.content.redstone.smartObserver.SmartObserverBlock;
import com.simibubi.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SmartObserverBlockEntity.class, remap = false)
public abstract class SmartObserverBlockEntityMixin extends SmartBlockEntity {
    @Shadow
    private InvManipulationBehaviour observedInventory;

    @Shadow
    private VersionedInventoryTrackerBehaviour invVersionTracker;

    @Shadow
    private boolean sustainSignal;

    @Shadow
    private TankManipulationBehaviour observedTank;

    @Shadow
    private FilteringBehaviour filtering;

    public SmartObserverBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(
        method = "tick",
        at = @At("HEAD"),
        cancellable = true
    )
    private void betterObserver(CallbackInfo ci) {
        ci.cancel();
        super.tick();
        SmartObserverBlockEntity self = (SmartObserverBlockEntity)(Object) this;

        if (level == null || level.isClientSide())
            return;

        BlockState state = getBlockState();
        if (self.turnOffTicks > 0) {
            self.turnOffTicks--;
            if (self.turnOffTicks == 0)
                level.scheduleTick(worldPosition, state.getBlock(), 1);
        }

        BlockPos targetPos = worldPosition.relative(SmartObserverBlock.getTargetDirection(state));
        BlockState blockState = level.getBlockState(targetPos);

        if (!filtering.getFilter().isEmpty()) {
            Item item = blockState.getBlock().asItem();
            if (item != null && filtering.test(new ItemStack(item))) {
                self.activate(3);
                return;
            }

            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty() && fluidState.isSource() &&
                filtering.test(new FluidStack(fluidState.getType(), 1))) {
                self.activate(3);
                return;
            }
        }

        // Detect fluids in pipe
        FluidTransportBehaviour fluidBehaviour =
            BlockEntityBehaviour.get(level, targetPos, FluidTransportBehaviour.TYPE);
        if (fluidBehaviour != null) {
            for (Direction side: Iterate.directions) {
                PipeConnection.Flow flow = fluidBehaviour.getFlow(side);
                if (flow == null || !flow.inbound || !flow.complete)
                    continue;
                if (!filtering.test(flow.fluid))
                    continue;
                self.activate();
                return;
            }
            return;
        }

        if (observedInventory.hasInventory()) {
            boolean skipInv = invVersionTracker.stillWaiting(observedInventory);
            invVersionTracker.awaitNewVersion(observedInventory);

            if (skipInv && sustainSignal)
                self.turnOffTicks = 6;

            if (!skipInv) {
                sustainSignal = false;
                IItemHandler handler = observedInventory.getInventory();
                if (handler != null) {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (!stack.isEmpty() && filtering.test(stack)) {
                            sustainSignal = true;
                            self.activate();
                            return;
                        }
                    }
                }
            }
        }

        if (!observedTank.hasInventory())
            return;
        IFluidHandler handler = observedTank.getInventory();
        if (handler != null) {
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack stack = handler.getFluidInTank(i);
                if (!stack.isEmpty() && filtering.test(stack)) {
                    self.activate();
                    return;
                }
            }
        }
    }
}
