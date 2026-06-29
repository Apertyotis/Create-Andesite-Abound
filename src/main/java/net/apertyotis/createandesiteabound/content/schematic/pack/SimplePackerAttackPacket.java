package net.apertyotis.createandesiteabound.content.schematic.pack;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.apertyotis.createandesiteabound.AllItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
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
                boolean closure = tag.getBoolean("Closure");
                tag.putBoolean("Closure", !closure);
                if (closure) {
                    RandomSource random = player.level().getRandom();
                    if (random.nextFloat() < 0.95f) {
                        tag.putInt("Variant", 0);
                    } else {
                        tag.putInt("Variant", random.nextIntBetweenInclusive(1, 4));
                    }
                }
                stack.setTag(tag);
            }
        });
        return true;
    }
}
