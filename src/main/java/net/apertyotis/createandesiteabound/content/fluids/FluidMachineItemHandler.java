package net.apertyotis.createandesiteabound.content.fluids;


import net.apertyotis.createandesiteabound.AllBlocks;
import net.apertyotis.createandesiteabound.AllConfig;
import net.apertyotis.createandesiteabound.foundation.CircularArray;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class FluidMachineItemHandler implements IItemHandlerModifiable {

    public final AbstractFluidMachineBlockEntity blockEntity;
    public final CircularArray<ItemStack> items;
    public boolean input;

    public FluidMachineItemHandler(AbstractFluidMachineBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        items = new CircularArray<>(ItemStack.class, AllConfig.fluid_machine_capacity);
    }

    public void onlyInput() {
        input = true;
    }

    public void onlyOutput() {
        input = false;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (items.isEmpty())
            items.addLast(stack);
        else
            items.set(0, stack);
        blockEntity.notifyUpdate();
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (items.isEmpty())
            return ItemStack.EMPTY;
        else if (input && items.size() < items.capacity())
            return ItemStack.EMPTY;
        else
            return items.getFirst();
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (input && items.size() < items.capacity() && !stack.isEmpty()) {
            if (!simulate) {
                items.addLast(stack.copyWithCount(1));
                blockEntity.notifyUpdate();
            }
            return stack.copyWithCount(stack.getCount() - 1);
        } else {
            return stack;
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (input || items.isEmpty() || amount < 1)
            return ItemStack.EMPTY;

        ItemStack stack = items.getFirst();
        if (!simulate) {
            items.removeLast();
            blockEntity.notifyUpdate();
        } else {
            stack = stack.copy();
        }
        return stack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return AllBlocks.FLUID_VESSEL.isIn(stack);
    }
}
