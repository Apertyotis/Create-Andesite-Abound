package net.apertyotis.createandesiteabound.mixin.create.foundation.item;

import com.simibubi.create.foundation.item.ItemHelper;
import net.apertyotis.createandesiteabound.foundation.VisitedItemStackTracker;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
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
                if (stackIn.isEmpty() || stackIn.getMaxStackSize() < amount || !test.test(stackIn))
                    continue;

                ItemStack extracted = inv.extractItem(i, stackIn.getCount(), true);
                if (extracted.isEmpty())
                    continue;

                VisitedItemStackTracker.SlotAmountRecord slotRecord = tracker.update(extracted, i);
                if (slotRecord.totalAmount >= amount) {
                    cir.setReturnValue(extracted.copyWithCount(amount));
                    if (!simulate) {
                        for (int j = 0; j < slotRecord.slots.size(); j++) {
                            amount -= inv.extractItem(slotRecord.slots.data[j], amount, false).getCount();
                        }
                    }
                    return;
                }
            }
        } else {
            VisitedItemStackTracker.SlotAmountRecord slotRecord = new VisitedItemStackTracker.SlotAmountRecord();
            ItemStack result = ItemStack.EMPTY;
            int maxExtractAmount = amount;
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stackIn = inv.getStackInSlot(i);
                if (stackIn.isEmpty() || (!result.isEmpty() && !ItemHandlerHelper.canItemStacksStack(result, stackIn)) ||
                        !test.test(stackIn))
                    continue;

                ItemStack extracted = inv.extractItem(i, stackIn.getCount(), simulate);
                if (!extracted.isEmpty())
                    continue;

                if (result.isEmpty()) {
                    result = extracted.copy();
                    maxExtractAmount = Math.min(amount, extracted.getMaxStackSize());
                }

                slotRecord.add(i, extracted.getCount());
                if (slotRecord.totalAmount >= maxExtractAmount)
                    break;
            }
            if (!result.isEmpty()) {
                amount = Math.min(slotRecord.totalAmount, maxExtractAmount);
                result.setCount(amount);
                cir.setReturnValue(result);
                if (!simulate) {
                    for (int j = 0; j < slotRecord.slots.size(); j++) {
                        int slot = slotRecord.slots.data[j];
                        int extractAmount = Math.min(inv.getStackInSlot(slot).getCount(), amount);
                        amount -= inv.extractItem(slot, extractAmount, false).getCount();
                    }
                }
                return;
            }
        }
        cir.setReturnValue(ItemStack.EMPTY);
    }
}
