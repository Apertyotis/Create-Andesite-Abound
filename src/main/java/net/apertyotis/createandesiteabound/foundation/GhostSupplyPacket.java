package net.apertyotis.createandesiteabound.foundation;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.apertyotis.createandesiteabound.AllTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

@SuppressWarnings("deprecation")
public class GhostSupplyPacket extends SimplePacketBase {
    final int slotId;
    final Item item;

    public GhostSupplyPacket(int slotId, Item item) {
        this.slotId = slotId;
        this.item = item;
    }

    public GhostSupplyPacket(FriendlyByteBuf buf) {
        this.slotId = buf.readChar();
        this.item = buf.readById(BuiltInRegistries.ITEM);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeChar(slotId);
        buf.writeId(BuiltInRegistries.ITEM, item);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null)
                return;
            if (!AllTags.AllItemTags.GHOST_SUPPLY.matches(item)) {
                return;
            }
            ItemStack stack = new ItemStack(item);
            stack.setCount(stack.getMaxStackSize());
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
