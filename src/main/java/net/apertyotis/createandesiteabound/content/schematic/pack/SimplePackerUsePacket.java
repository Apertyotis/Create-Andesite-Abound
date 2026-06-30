package net.apertyotis.createandesiteabound.content.schematic.pack;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.apertyotis.createandesiteabound.AllItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;

import java.nio.file.Path;
import java.util.List;

public class SimplePackerUsePacket extends SimplePacketBase {

    public BlockPos anchor;
    public BlockPos size;
    public String filename;

    public SimplePackerUsePacket(BlockPos anchor, BlockPos size, String filename) {
        this.anchor = anchor;
        this.size = size;
        this.filename = filename;
    }

    public SimplePackerUsePacket(FriendlyByteBuf buffer) {
        anchor = buffer.readBlockPos();
        size = buffer.readBlockPos();
        filename = buffer.readUtf();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeBlockPos(size);
        buffer.writeUtf(filename);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null)
                return;
            ItemStack stack = player.getMainHandItem();
            if (!AllItems.SIMPLE_PACKER.isIn(stack))
                return;

            Level world = player.level();

            // noinspection DataFlowIssue
            if (stack.hasTag() && stack.getTag().getBoolean("Closure")) {
                Path path = StructureHelper.getOrCreateServerTempSchematicPath((ServerLevel) world)
                    .resolve(player.getGameProfile().getName());
                filename = StructureHelper.getValidFilename(path, filename, true);
                if (StructureHelper.saveTempSchematic(path, filename, world, anchor, size)) {
                    ItemStack schematic = AllItems.SIMPLE_SCHEMATIC.asStack();
                    CompoundTag tag = schematic.getOrCreateTag();
                    tag.putString("File", filename);
                    tag.putBoolean("Temp", true);
                    player.getInventory().placeItemBackInInventory(schematic);
                    AllSoundEvents.CONFIRM.playFrom(player);

                    BlockPos corner = anchor.offset(size).offset(-1, -1, -1);
                    StructureHelper.destroyStructure(world, BlockPos.betweenClosed(anchor, corner));
                    for (SuperGlueEntity glue: SuperGlueEntity.collectCropped(world, new AABB(anchor, corner))) {
                        glue.discard();
                    }
                } else {
                    AllSoundEvents.DENY.playFrom(player);
                    player.displayClientMessage(Component.translatable("caa.packer.error.fail")
                        .withStyle(ChatFormatting.RED), true);
                }
                return;
            }

            StructureMetaCache.matchAnyStructure(world, anchor, size, (path, blockReader) -> {
                List<BlockPos> blockPosList = blockReader.getBlockMap().entrySet().stream()
                    .filter(e -> !e.getValue().is(Blocks.AIR))
                    .map(e -> anchor.offset(e.getKey()))
                    .toList();
                StructureHelper.destroyStructure(world, blockPosList);

                blockReader.getEntityStream().forEach(entity -> {
                    AABB bounds = entity.getBoundingBox().move(anchor);
                    world.getEntitiesOfClass(entity.getClass(), bounds)
                        .stream().findAny().ifPresent(Entity::discard);
                });

                ItemStack schematic = AllItems.SIMPLE_SCHEMATIC.asStack();
                CompoundTag tag = new CompoundTag();
                tag.putString("File", path.toString().replace("\\", "/"));
                schematic.setTag(tag);
                player.getInventory().placeItemBackInInventory(schematic);
                AllSoundEvents.CONFIRM.playFrom(player);
            }, result -> {
                String key = switch (result) {
                    case SUCCESS -> "";
                    case SIZE_ERROR -> "caa.packer.error.size";
                    case BLOCK_ERROR -> "caa.packer.error.block";
                };
                if (key.isEmpty())
                    return;
                AllSoundEvents.DENY.playFrom(player);
                player.displayClientMessage(Component.translatable(key)
                    .withStyle(ChatFormatting.RED), true);
            });
        });
        return true;
    }
}
