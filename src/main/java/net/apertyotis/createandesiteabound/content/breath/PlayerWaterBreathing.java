package net.apertyotis.createandesiteabound.content.breath;

import net.apertyotis.createandesiteabound.AllConfig;
import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 防止玩家溺水，防止墙内窒息通过重写isInWall方法实现
 * @see net.apertyotis.createandesiteabound.mixin.minecraft.noclip.PlayerMixin
 */
@Mod.EventBusSubscriber(modid = CreateAndesiteAbound.MOD_ID)
public class PlayerWaterBreathing {
    // 玩家不会在水下窒息
    @SubscribeEvent
    public static void onLivingBreath(LivingBreatheEvent event) {
        if (!AllConfig.player_can_breath)
            return;

        if (event.getEntity() instanceof Player)
            event.setCanBreathe(true);
    }
}
