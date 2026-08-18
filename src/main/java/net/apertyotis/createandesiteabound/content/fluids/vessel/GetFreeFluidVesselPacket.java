package net.apertyotis.createandesiteabound.content.fluids.vessel;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.apertyotis.createandesiteabound.AllBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class GetFreeFluidVesselPacket extends SimplePacketBase {
    final int slotId;

    public GetFreeFluidVesselPacket(int slotId) {
        this.slotId = slotId;
    }

    public GetFreeFluidVesselPacket(FriendlyByteBuf buf) {
        this.slotId = buf.readChar();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeChar(slotId);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null)
                return;
            ItemStack stack = AllBlocks.FLUID_VESSEL.asStack(64);
            if (slotId >= 0 && slotId < 36) {
                if (player.getInventory().items.get(slotId).isEmpty()) {
                    player.getInventory().items.set(slotId, stack);
                    player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, slotId, stack));
                }
            } else if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            }
        });
        return true;
    }
}
