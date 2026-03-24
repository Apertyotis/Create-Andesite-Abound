package net.apertyotis.createandesiteabound.foundation;

import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

// 从 Create 6.0 移植，用于优化保险库性能
public class SameSizeCombinedInvWrapper extends CombinedInvWrapper {
    private final int numSlotsPerInv;
    private final int numCombinedSlots;

    private SameSizeCombinedInvWrapper(int numSlotsPerInv, IItemHandlerModifiable... itemHandler) {
        super(itemHandler);

        this.numSlotsPerInv = numSlotsPerInv;
        this.numCombinedSlots = numSlotsPerInv * itemHandler.length;
    }

    /**
     * Create a SameSizeCombinedInvWrapper if all item handlers actually have the same size.
     * Otherwise, falls back to the parent class.
     */
    public static CombinedInvWrapper create(IItemHandlerModifiable... itemHandler) {
        if (itemHandler.length == 0) {
            // No need to subclass here.
            // Early out because we need to validate that all slots have the same length.
            return new CombinedInvWrapper(itemHandler);
        }

        // If any inventories have different slot counts, fall back to the default impl.
        int firstInvNumSlots = itemHandler[0].getSlots();
        for (int i = 1; i < itemHandler.length; i++) {
            if (firstInvNumSlots != itemHandler[i].getSlots()) {
                return new CombinedInvWrapper(itemHandler);
            }
        }

        return new SameSizeCombinedInvWrapper(firstInvNumSlots, itemHandler);
    }

    @Override
    protected int getIndexForSlot(int slot) {
        // The parent class agrees than -1 means invalid input.
        if (slot < 0 || slot >= numCombinedSlots) {
            return -1;
        }

        // Floor div go brr.
        return slot / numSlotsPerInv;
    }
}
