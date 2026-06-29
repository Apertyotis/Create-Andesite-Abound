package net.apertyotis.createandesiteabound.content.schematic.pack;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.apertyotis.createandesiteabound.AllItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class SimplePackerAttackPacket extends SimplePacketBase {

    public SimplePackerAttackPacket() {}
    public SimplePackerAttackPacket(FriendlyByteBuf ignored) {}

    @Override
    public void write(FriendlyByteBuf buffer) {}

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player == null)
                return;
            ItemStack stack = player.getMainHandItem();
            if (AllItems.SIMPLE_PACKER.isIn(stack)) {
                CompoundTag tag = stack.hasTag() ? stack.getTag() : new CompoundTag();
                // noinspection DataFlowIssue
                tag.putBoolean("Closure", !tag.getBoolean("Closure"));
                stack.setTag(tag);
            }
        });
        return true;
    }
}
