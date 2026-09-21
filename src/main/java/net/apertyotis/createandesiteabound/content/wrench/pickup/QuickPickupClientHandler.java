package net.apertyotis.createandesiteabound.content.wrench.pickup;

import net.apertyotis.createandesiteabound.AllPackets;
import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.apertyotis.createandesiteabound.compat.Mods;
import net.apertyotis.createandesiteabound.compat.ftbultimine.QuickPickupUltimineHandler;
import net.apertyotis.createandesiteabound.foundation.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateAndesiteAbound.MOD_ID, value = Dist.CLIENT)
public class QuickPickupClientHandler {
    public static final int DAS = 250;
    public static long timestamp = -1;

    public static boolean onMouseInput(int button, boolean pressed) {
        if (!pressed && button == ClientEvents.getKeyAttackCode()) {
            timestamp = -1;
        }
        return false;
    }

    @SubscribeEvent
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        switch (event.getAction()) {
            case START, CLIENT_HOLD -> {
                if (timestamp == -1)
                    timestamp = System.currentTimeMillis();
                else if (timestamp + DAS >= System.currentTimeMillis())
                    return;
            } default -> {
                return;
            }
        }
        boolean isUltimineActivated = Mods.FTBUltimine
            .runIfInstalled(() -> QuickPickupUltimineHandler::isUltimineActivated)
            .orElse(false);
        if (isUltimineActivated || !QuickPickupUtil.canWrench(event))
            return;

        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
            AllPackets.getChannel().sendToServer(new QuickPickupPacket(hit));
            event.setCanceled(true);
        }
    }
}
