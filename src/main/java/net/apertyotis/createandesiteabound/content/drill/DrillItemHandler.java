package net.apertyotis.createandesiteabound.content.drill;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;

public class DrillItemHandler implements IItemHandler {
    public final Queue<ItemStack> items = new ArrayDeque<>();
    public final SmartBlockEntity be;

    public DrillItemHandler(SmartBlockEntity be) {
        this.be = be;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        ItemStack stack = items.peek();
        return stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = simulate ? items.peek() : items.poll();
        if (!simulate && stack != null)
            be.notifyUpdate();
        return stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }
}
