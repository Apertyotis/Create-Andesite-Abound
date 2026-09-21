package net.apertyotis.createandesiteabound.content.wrench.pickup;

import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.apertyotis.createandesiteabound.compat.Mods;
import net.apertyotis.createandesiteabound.compat.ftbultimine.QuickPickupUltimineHandler;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateAndesiteAbound.MOD_ID)
public class QuickPickupUltimine {
    @SubscribeEvent
    public static void onWrenchUltimine(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide)
            return;
        Mods.FTBUltimine.executeIfInstalled(() -> () -> QuickPickupUltimineHandler.onWrenchUltimine(event));
    }
}
