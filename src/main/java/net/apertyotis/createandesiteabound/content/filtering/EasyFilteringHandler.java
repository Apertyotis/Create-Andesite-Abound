package net.apertyotis.createandesiteabound.content.filtering;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.SidedFilteringBehaviour;
import com.simibubi.create.foundation.utility.RaycastHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EasyFilteringHandler {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onWrenchClickFilterSlot(PlayerInteractEvent.RightClickBlock event) {
        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        if (player == null || player.isSpectator() || player.isShiftKeyDown() || player instanceof FakePlayer)
            return;
        if (!AllItems.WRENCH.isIn(player.getMainHandItem()))
            return;

        if (event.getSide().isClient()) {
            Boolean result = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () ->
                EasyFilteringHandlerClient.EASY_FILTERING_HANDLER_CLIENT.isInteracting(pos));
            if (result != null && result) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
        }

        FilteringBehaviour behaviour = BlockEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
        if (behaviour == null || !behaviour.isActive())
            return;

        BlockHitResult ray = RaycastHelper.rayTraceRange(world, player, player.getBlockReach());
        if (ray == null)
            return;
        if (behaviour instanceof SidedFilteringBehaviour sidedBehaviour) {
            behaviour = sidedBehaviour.get(ray.getDirection());
            if (behaviour == null)
                return;
        }
        if (behaviour.testHit(ray.getLocation())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            if (event.getSide().isClient()) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    EasyFilteringHandlerClient.EASY_FILTERING_HANDLER_CLIENT.startInteraction(pos, ray.getDirection()));
            }
        }
    }
}
