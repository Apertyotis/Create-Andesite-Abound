package net.apertyotis.createandesiteabound.content.wrench.pickup;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkEvent;

public class QuickPickupPacket extends SimplePacketBase {
    private final BlockHitResult hitResult;

    public QuickPickupPacket(BlockHitResult hitResult) {
        this.hitResult = hitResult;
    }

    public QuickPickupPacket(FriendlyByteBuf buf) {
        hitResult = buf.readBlockHitResult();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockHitResult(hitResult);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null)
                return;
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = hitResult.getBlockPos();
            BlockState blockState = level.getBlockState(pos);
            if (blockState.getBlock() instanceof IWrenchable wrenchable) {
                wrenchable.onSneakWrenched(blockState, new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult));
            } else if (AllTags.AllBlockTags.WRENCH_PICKUP.matches(blockState)) {
                if (!player.isCreative()) {
                    Block.getDrops(blockState, level, pos, level.getBlockEntity(pos), player, player.getMainHandItem())
                        .forEach(itemStack -> player.getInventory().placeItemBackInInventory(itemStack));
                }
                blockState.spawnAfterBreak(level, pos, ItemStack.EMPTY, true);
                level.destroyBlock(pos, false);
                AllSoundEvents.WRENCH_REMOVE.playOnServer(level, pos, 1, level.getRandom().nextFloat() * .5f + .5f);
            }
        });
        return true;
    }
}
