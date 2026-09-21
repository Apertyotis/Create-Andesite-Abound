package net.apertyotis.createandesiteabound.compat.curios;

import net.apertyotis.createandesiteabound.AllItems;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

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
}
