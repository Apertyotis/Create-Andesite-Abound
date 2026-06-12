package net.apertyotis.createandesiteabound.mixin.create.contraption;

import com.simibubi.create.content.contraptions.Contraption.ContraptionInvWrapper;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 移植自 Create 6.0，大幅优化动态结构上多存储容器的查找性能<br>
 * 详见 Create PR<a href="https://github.com/Creators-of-Create/Create/pull/9706">#9706</a>
 */
@Mixin(value = ContraptionInvWrapper.class, remap = false)
public abstract class ContraptionInvWrapperMixin extends CombinedInvWrapper {
    // Lookup arrays
    @Unique
    private int[] caa$slotToStorage;   // Maps each slot to its storage index
    @Unique
    private int[] caa$slotOffsets;     // Starting slot for each storage

    @Inject(method = "<init>(Z[Lnet/minecraftforge/items/IItemHandlerModifiable;)V", at = @At("RETURN"))
    private void MountedItemStorageWrapper(boolean isExternal, IItemHandlerModifiable[] itemHandler, CallbackInfo ci) {
        // Build lookup arrays
        int totalSlots = getSlots();
        caa$slotToStorage = new int[totalSlots];
        caa$slotOffsets = new int[itemHandler.length];

        int currentSlot = 0;
        for (int storageIdx = 0; storageIdx < itemHandler.length; storageIdx++) {
            caa$slotOffsets[storageIdx] = currentSlot;
            int slotsInStorage = itemHandler[storageIdx].getSlots();

            for (int i = 0; i < slotsInStorage; i++) {
                caa$slotToStorage[currentSlot + i] = storageIdx;
            }

            currentSlot += slotsInStorage;
        }
    }

    @Override
    protected int getIndexForSlot(int slot) {
        if (slot < 0 || slot >= caa$slotToStorage.length) {
            return -1;
        }
        return caa$slotToStorage[slot];
    }

    @Override
    protected int getSlotFromIndex(int slot, int index) {
        return slot - caa$slotOffsets[index];
    }
}
