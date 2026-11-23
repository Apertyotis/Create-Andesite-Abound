package net.apertyotis.createandesiteabound.compat.beyonddimensions;

import com.wintercogs.beyonddimensions.api.dimensionnet.DimensionsNet;
import com.wintercogs.beyonddimensions.api.dimensionnet.UnifiedStorage;
import com.wintercogs.beyonddimensions.api.storage.key.KeyAmount;
import com.wintercogs.beyonddimensions.api.storage.key.impl.FluidStackKey;
import com.wintercogs.beyonddimensions.api.storage.key.impl.ItemStackKey;
import net.apertyotis.createandesiteabound.compat.Mods;
import net.apertyotis.createandesiteabound.compat.sophisticatedbackpacks.Backpacks;
import net.apertyotis.createandesiteabound.content.schematic.wrapper.SimpleWrapperChecklist;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidType;

import java.util.ArrayList;
import java.util.List;

public class BeyondDimensions {
    public static void gatherMaterialsFromNetwork(SimpleWrapperChecklist checklist, Player player) {
        DimensionsNet net = DimensionsNet.getPrimaryNetFromPlayer(player);
        if (net == null)
            return;
        UnifiedStorage storage = net.getUnifiedStorage();
        List<ItemStack> overflowing = new ArrayList<>();
        for (int i = 0; i < storage.getSlots() && !checklist.isComplete(); i++) {
            KeyAmount ka = storage.getStackBySlot(i);
            if (ka.key() instanceof ItemStackKey itemKey) {
                if (itemKey.isEmpty())
                    continue;
                boolean changed = false;
                if (!checklist.isItemComplete()) {
                    ItemStack inSlot = itemKey.copyStackWithCount(ka.amount());
                    SimpleWrapperChecklist.ConsumeResult result = checklist.countAndConsumeItem(player, inSlot);
                    if (result.changed()) {
                        changed = true;
                        ItemStack remaining = result.remaining();
                        remaining.grow(1);
                        ItemStackKey newKey = new ItemStackKey(remaining);
                        long newAmount = ka.amount() - inSlot.getCount() + remaining.getCount() - 1;
                        storage.setStackDirectly(i, newKey, newAmount);
                        overflowing.addAll(result.overflowing());
                        if (newAmount <= 0) {
                            i--;
                            continue;
                        }
                    }
                }

                if (changed) {
                    ka = storage.getStackBySlot(i);
                    if (ka.key() instanceof ItemStackKey)
                        itemKey = (ItemStackKey) ka.key();
                    else
                        continue;
                }
                ItemStack inSlot = itemKey.copyStackWithCount(ka.amount());
                boolean isBackpack = Mods.SophisticatedBackpacks.runIfInstalled(
                    () -> () -> Backpacks.isBackpack(inSlot)).orElse(false);
                if (!checklist.isFluidComplete() && !isBackpack) {
                    SimpleWrapperChecklist.ConsumeResult result = checklist.drainItem(inSlot);
                    if (result.changed()) {
                        ItemStack remaining = result.remaining();
                        remaining.grow(1);
                        ItemStackKey newKey = new ItemStackKey(remaining);
                        long newAmount = ka.amount() - inSlot.getCount() + remaining.getCount() - 1;
                        storage.setStackDirectly(i, newKey, newAmount);
                        overflowing.addAll(result.overflowing());
                        if (newAmount <= 0)
                            i--;
                    }
                }
            } else if (ka.key() instanceof FluidStackKey fluidKey) {
                if (fluidKey.isEmpty() || checklist.isFluidComplete())
                    continue;
                SimpleWrapperChecklist.CheckEntry entry = checklist.fluidMap.get(fluidKey.getSource());
                if (entry == null || entry.isComplete())
                    continue;
                long cost = Math.min(ka.amount() / 1000, entry.total - entry.count);
                KeyAmount extracted = storage.extract(i, cost * FluidType.BUCKET_VOLUME, true);
                if (extracted.amount() != cost * FluidType.BUCKET_VOLUME)
                    continue;
                storage.extract(i, cost * FluidType.BUCKET_VOLUME, false);
                entry.count += (int) cost;
                if (entry.isComplete()) {
                    checklist.completeFluidEntries++;
                }
                if (ka.amount() <= cost * FluidType.BUCKET_VOLUME)
                    i--;
            }
        }
        Inventory playerInv = player.getInventory();
        for (ItemStack stack: overflowing) {
            ItemStackKey key = new ItemStackKey(stack);
            KeyAmount ka = storage.insert(key, stack.getCount(), false);
            if (!ka.isEmpty()) {
                playerInv.placeItemBackInInventory((ItemStack) ka.key().copyStackWithCount(ka.amount()));
            }
        }
    }
}
