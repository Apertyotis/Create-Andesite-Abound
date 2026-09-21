package net.apertyotis.createandesiteabound.content.wrench.pickup;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;

public class QuickPickupUtil {

    public static boolean canWrench(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (event.getHand() != InteractionHand.MAIN_HAND)
            return false;
        ItemStack inHand = player.getMainHandItem();
        if (!AllItems.WRENCH.isIn(inHand) || !AllTags.AllItemTags.WRENCH.matches(inHand.getItem()))
            return false;

        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        return state.getBlock() instanceof IWrenchable || AllTags.AllBlockTags.WRENCH_PICKUP.matches(state);
    }

    public static void wrenchOn(ServerPlayer player, BlockHitResult hitResult) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos pos = hitResult.getBlockPos();
        BlockState blockState = level.getBlockState(pos);
        if (blockState.getBlock() instanceof IWrenchable wrenchable) {
            wrenchable.onSneakWrenched(blockState, new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult));
        } else if (AllTags.AllBlockTags.WRENCH_PICKUP.matches(blockState)) {
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, blockState, player);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled())
                return;
            if (!player.isCreative()) {
                Block.getDrops(blockState, level, pos, level.getBlockEntity(pos), player, player.getMainHandItem())
                    .forEach(itemStack -> player.getInventory().placeItemBackInInventory(itemStack));
            }
            blockState.spawnAfterBreak(level, pos, ItemStack.EMPTY, true);
            level.destroyBlock(pos, false);
            AllSoundEvents.WRENCH_REMOVE.playOnServer(level, pos, 1, level.getRandom().nextFloat() * .5f + .5f);
        }
    }
}
