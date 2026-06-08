package net.apertyotis.createandesiteabound.mixin.create.foundation.item;

import com.simibubi.create.foundation.item.ItemHelper;
import net.apertyotis.createandesiteabound.foundation.VisitedItemStackTracker;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(value = ItemHelper.class, remap = false)
public abstract class ItemHelperMixin {

    // 重写漏斗取物逻辑 (尤其是黄铜漏斗)
    @Inject(
            method = "extract(Lnet/minecraftforge/items/IItemHandler;Ljava/util/function/Predicate;Lcom/simibubi/create/foundation/item/ItemHelper$ExtractionCountMode;IZ)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void quickSlot(
            IItemHandler inv, Predicate<ItemStack> test, ItemHelper.ExtractionCountMode mode,
            int amount, boolean simulate, CallbackInfoReturnable<ItemStack> cir
    ) {
        if (mode == ItemHelper.ExtractionCountMode.EXACTLY) {
            VisitedItemStackTracker tracker = new VisitedItemStackTracker();
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stackIn = inv.getStackInSlot(i);
                if (stackIn.isEmpty() || !test.test(stackIn))
                    continue;

                ItemStack extracted = inv.extractItem(i, amount, true);
                if (extracted.isEmpty())
                    continue;

                VisitedItemStackTracker.SlotAmountRecord slotRecord = tracker.update(stackIn, i);
                if (slotRecord.getTotalAmount() >= amount) {
                    cir.setReturnValue(extracted.copyWithCount(amount));
                    if (!simulate) {
                        for (int slot: slotRecord.getSlotsIndex()) {
                            amount -= inv.extractItem(slot, amount, false).getCount();
                        }
                    }
                    return;
                }
            }
        } else {
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stackIn = inv.getStackInSlot(i);
                if (stackIn.isEmpty() || !test.test(stackIn))
                    continue;

                ItemStack extracted = inv.extractItem(i, amount, simulate);
                if (!extracted.isEmpty()) {
                    cir.setReturnValue(extracted);
                    return;
                }
            }
        }
        cir.setReturnValue(ItemStack.EMPTY);
    }
}
