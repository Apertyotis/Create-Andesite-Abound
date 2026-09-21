package net.apertyotis.createandesiteabound.compat.ftbultimine;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import net.apertyotis.createandesiteabound.content.wrench.pickup.QuickPickupUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class QuickPickupUltimineHandler {

    @OnlyIn(Dist.CLIENT)
    public static boolean isUltimineActivated() {
        return FTBUltimineClient.keyBinding.isDown();
    }

    public static void onWrenchUltimine(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START)
            return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        FTBUltiminePlayerData data = FTBUltimine.instance.getOrCreatePlayerData(player);
        if (!data.isPressed())
            return;
        if (!QuickPickupUtil.canWrench(event))
            return;

        HitResult hitResult = FTBUltiminePlayerData.rayTrace(player);
        if (!(hitResult instanceof BlockHitResult hit) || hitResult.getType() != HitResult.Type.BLOCK)
            return;

        data.clearCache();
        data.updateBlocks(player, event.getPos(), hit.getDirection(), false, FTBUltimineServerConfig.getMaxBlocks(player));
        if (!data.hasCachedPositions())
            return;
        data.setPressed(false);
        for (BlockPos pos: data.cachedPositions()) {
            BlockHitResult currentHit = hit.withPosition(pos);
            QuickPickupUtil.wrenchOn(player, currentHit);
        }
        data.setPressed(true);
    }
}
