package net.apertyotis.createandesiteabound.content.wrench.pickup;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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
            QuickPickupUtil.wrenchOn(player, hitResult);
        });
        return true;
    }
}
