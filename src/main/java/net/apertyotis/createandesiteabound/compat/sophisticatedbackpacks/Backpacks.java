package net.apertyotis.createandesiteabound.compat.sophisticatedbackpacks;

import net.apertyotis.createandesiteabound.compat.Mods;
import net.apertyotis.createandesiteabound.compat.curios.Curios;
import net.apertyotis.createandesiteabound.content.schematic.wrapper.SimpleWrapperChecklist;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageFluidHandler;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;

public class Backpacks {
    public static boolean isBackpack(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BackpackItem;
    }

    public static void gatherMaterialsFromBackpacks(SimpleWrapperChecklist checklist, Player player) {
        ItemStack armor = player.getInventory().getArmor(2);
        gatherMaterialsFromBackpack(checklist, player, armor);
        Mods.Curios.executeIfInstalled(() -> () -> Curios.forEach(player,
            stack -> gatherMaterialsFromBackpack(checklist, player, stack)));
    }

    public static void gatherMaterialsFromBackpack(SimpleWrapperChecklist checklist, Player player, ItemStack backpack) {
        if (!isBackpack(backpack) || checklist.isComplete())
            return;
        IStorageWrapper handler = backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance())
            .resolve().orElse(null);
        if (handler == null)
            return;
        InventoryHandler inventory = handler.getInventoryHandler();
        checklist.gatherItemsFrom(player, inventory, stack -> !isBackpack(stack), items -> {
            Inventory playerInv = player.getInventory();
            for (ItemStack stack: items) {
                ItemStack remaining = inventory.insertItem(stack, false);
                if (!remaining.isEmpty())
                    playerInv.placeItemBackInInventory(stack);
            }
        });

        if (checklist.isFluidComplete())
            return;
        IStorageFluidHandler tank = handler.getFluidHandler().orElse(null);
        checklist.drainFluidsFrom(tank);
    }
}
