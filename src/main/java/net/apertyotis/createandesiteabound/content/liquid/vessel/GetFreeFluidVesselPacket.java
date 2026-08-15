package net.apertyotis.createandesiteabound.content.liquid.vessel;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.apertyotis.createandesiteabound.AllBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

public class GetFreeFluidVesselPacket extends SimplePacketBase {
    public GetFreeFluidVesselPacket() {}

    public GetFreeFluidVesselPacket(FriendlyByteBuf ignored) {}

    @Override
    public void write(FriendlyByteBuf ignored) {}

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.getMainHandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, AllBlocks.FLUID_VESSEL.asStack(64));
            }
        });
        return true;
    }
}
