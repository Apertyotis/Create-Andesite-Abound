package net.apertyotis.createandesiteabound.compat.curios;

import net.apertyotis.createandesiteabound.AllItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.function.Consumer;

public class Curios {
    public static boolean isWearingBottledGhost(Player player) {
        ICuriosItemHandler handler = player.getCapability(CuriosCapability.INVENTORY).resolve().orElse(null);
        if (handler == null)
            return false;
        for (ICurioStacksHandler curios: handler.getCurios().values()) {
            for (int i = 0; i < curios.getSlots(); i++) {
                if (AllItems.BOTTLED_GHOST.isIn(curios.getStacks().getStackInSlot(i)))
                    return true;
            }
        }
        return false;
    }

    public static void forEach(Player player, Consumer<ItemStack> consumer) {
        ICuriosItemHandler handler = player.getCapability(CuriosCapability.INVENTORY).resolve().orElse(null);
        if (handler == null)
            return;
        for (ICurioStacksHandler curios: handler.getCurios().values()) {
            for (int i = 0; i < curios.getSlots(); i++) {
                consumer.accept(curios.getStacks().getStackInSlot(i));
            }
        }
    }
}
