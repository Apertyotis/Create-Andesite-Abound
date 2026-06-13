package net.apertyotis.createandesiteabound.content.hachimi_glue;

import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;

public class HachimiGlueModificationPacket extends SimplePacketBase {
    private final int entityId;
    private final AABB aabb;

    public HachimiGlueModificationPacket(int entityId, AABB aabb) {
        this.entityId = entityId;
        this.aabb = aabb;
    }

    public HachimiGlueModificationPacket(FriendlyByteBuf buffer) {
        entityId = buffer.readInt();
        BlockPos first = buffer.readBlockPos();
        BlockPos second = buffer.readBlockPos();
        aabb = new AABB(first, second);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeBlockPos(BlockPos.containing(aabb.minX, aabb.minY, aabb.minZ));
        buffer.writeBlockPos(BlockPos.containing(aabb.maxX, aabb.maxY, aabb.maxZ));
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null)
                return;
            if (!AllConfig.hachimi_glue) {
                player.displayClientMessage(Component.translatable("info.caa.option.hachimi_disabled")
                        .withStyle(ChatFormatting.RED), true);
                return;
            }
            // noinspection resource
            Entity entity = player.level().getEntity(entityId);
            if (!(entity instanceof SuperGlueEntity glue))
                return;
            double range = 32;
            if (player.distanceToSqr(glue.position()) > range * range)
                return;
            glue.setBoundingBox(aabb);
        });
        return true;
    }
}
