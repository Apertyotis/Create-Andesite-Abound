package net.apertyotis.createandesiteabound.content.filtering;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class EasyFilteringPacket extends SimplePacketBase {

    private final BlockPos pos;
    private final Direction side;
    private final ItemStack filter;

    public EasyFilteringPacket(BlockPos pos, Direction side, ItemStack filter) {
        this.pos = pos;
        this.side = side;
        this.filter = filter;
    }

    public EasyFilteringPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        side = buf.readEnum(Direction.class);
        filter = buf.readItem();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeEnum(side);
        buf.writeItem(filter);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null)
                return;
            Level level = player.level();
            FilteringBehaviour behaviour = BlockEntityBehaviour.get(level, pos, FilteringBehaviour.TYPE);
            if (behaviour == null)
                return;

            if (filter.getItem() instanceof FilterItem || !behaviour.setFilter(side, filter)) {
                player.displayClientMessage(Lang.translateDirect("logistics.filter.invalid_item"), true);
                AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
                return;
            }
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
        });
        return true;
    }
}
