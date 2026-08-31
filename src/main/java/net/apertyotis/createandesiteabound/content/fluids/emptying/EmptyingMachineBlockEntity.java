package net.apertyotis.createandesiteabound.content.fluids.emptying;

import net.apertyotis.createandesiteabound.AllBlocks;
import net.apertyotis.createandesiteabound.content.fluids.AbstractFluidMachineBlockEntity;
import net.apertyotis.createandesiteabound.content.fluids.vessel.FluidVesselItem;
import net.apertyotis.createandesiteabound.foundation.CircularArray;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;

public class EmptyingMachineBlockEntity extends AbstractFluidMachineBlockEntity {

    public EmptyingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory.onlyInput();
    }

    @Override
    public Direction getTarget() {
        return Direction.DOWN;
    }

    @Override
    public void tick() {
        super.tick();
        cooldown--;
        if (cooldown > 0)
            return;
        cooldown = 5;

        boolean changed = false;
        CircularArray<ItemStack> items = inventory.items;
        AutoInput:
        if (items.size() < items.capacity() && targetInv.hasInventory() && !invVersionTracker.stillWaiting(targetInv)) {
            IItemHandler handler = targetInv.getInventory();
            if (handler == null)
                break AutoInput;
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack.isEmpty() || !AllBlocks.FLUID_VESSEL.isIn(stack))
                    continue;
                int amount = Math.min(items.capacity() - items.size(), stack.getCount());
                ItemStack extracted = handler.extractItem(i, amount, false);
                if (extracted.isEmpty())
                    continue;
                for (int j = 0; j < extracted.getCount(); j++) {
                    items.addLast(extracted.copyWithCount(1));
                    changed = true;
                    if (items.size() == items.capacity())
                        break AutoInput;
                }
            }
        }

        if (changed) {
            notifyUpdate();
            invWrapper.incrementVersion();
            invVersionTracker.awaitNewVersion(targetInv);
        }

        if (items.isEmpty() || !targetTank.hasInventory())
            return;
        IFluidHandler handler = targetTank.getInventory();
        if (handler == null)
            return;

        changed = false;
        while (!items.isEmpty()) {
            ItemStack stack = items.getFirst();
            FluidStack fluid = FluidVesselItem.getFluid(stack);
            if (fluid.isEmpty()) {
                items.removeFirst();
                changed = true;
            } else if (handler.fill(fluid, FluidAction.SIMULATE) == fluid.getAmount()) {
                handler.fill(fluid, FluidAction.EXECUTE);
                items.removeFirst();
                changed = true;
            } else {
                break;
            }
        }
        if (changed) {
            notifyUpdate();
            invWrapper.incrementVersion();
            if (level instanceof ServerLevel)
                level.playSound(null, worldPosition, SoundEvents.BUCKET_EMPTY,
                    SoundSource.BLOCKS, .3f, 1f);
        }
    }
}
