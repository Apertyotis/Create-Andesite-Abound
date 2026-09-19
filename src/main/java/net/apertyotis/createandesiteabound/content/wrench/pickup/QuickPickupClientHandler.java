package net.apertyotis.createandesiteabound.content.wrench.pickup;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.apertyotis.createandesiteabound.AllPackets;
import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateAndesiteAbound.MOD_ID, value = Dist.CLIENT)
public class QuickPickupClientHandler {

    @SubscribeEvent
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND)
            return;
        Player player = event.getEntity();
        ItemStack inHand = player.getMainHandItem();
        if (!AllItems.WRENCH.isIn(inHand) || !AllTags.AllItemTags.WRENCH.matches(inHand.getItem()))
            return;
        BlockState blockState = event.getLevel().getBlockState(event.getPos());
        if (blockState.getBlock() instanceof IWrenchable || AllTags.AllBlockTags.WRENCH_PICKUP.matches(blockState)) {
            HitResult hitResult = Minecraft.getInstance().hitResult;
            if (hitResult instanceof BlockHitResult hit && hit.getType() != HitResult.Type.MISS) {
                AllPackets.getChannel().sendToServer(new QuickPickupPacket(hit));
                event.setCanceled(true);
            }
        }
    }
}
