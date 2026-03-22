package net.apertyotis.createandesiteabound.content.schematic;

import com.simibubi.create.content.schematics.packet.SchematicSyncPacket;
import net.apertyotis.createandesiteabound.AllItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraftforge.network.NetworkEvent;

public class SimpleSchematicSyncPacket extends SchematicSyncPacket {

    public SimpleSchematicSyncPacket(int slot, StructurePlaceSettings settings, BlockPos anchor, boolean deployed) {
        super(slot, settings, anchor, deployed);
    }

    public SimpleSchematicSyncPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null)
                return;
            ItemStack stack;
            if (slot == -1) {
                stack = player.getMainHandItem();
            } else {
                stack = player.getInventory().getItem(slot);
            }
            if (!AllItems.SIMPLE_SCHEMATIC.isIn(stack)) {
                return;
            }
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean("Deployed", deployed);
            tag.put("Anchor", NbtUtils.writeBlockPos(anchor));
            tag.putString("Rotation", rotation.name());
            tag.putString("Mirror", mirror.name());
        });
        return true;
    }
}
