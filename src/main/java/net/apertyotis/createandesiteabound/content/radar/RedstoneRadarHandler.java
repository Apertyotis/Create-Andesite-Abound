package net.apertyotis.createandesiteabound.content.radar;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.RaycastHelper;
import net.apertyotis.createandesiteabound.AllBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class RedstoneRadarHandler {

    public static final RedstoneRadarHandler REDSTONE_RADAR_HANDLER = new RedstoneRadarHandler();

    private static final Object outlineSlot = new Object();

    public void tick() {
        Level world = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        if (world == null || player == null)
            return;

        ItemStack heldItem = player.getMainHandItem();
        if (!AllBlocks.REDSTONE_RADAR.isIn(heldItem))
            return;

        CompoundTag tag = heldItem.getTag();
        if (tag != null && tag.contains("TargetPos") && tag.contains("TargetDimension")) {
            BlockPos targetPos = NbtUtils.readBlockPos(tag.getCompound("TargetPos"));
            String targetDimension = tag.getString("TargetDimension");

            if (world.dimensionTypeId().location().toString().equals(targetDimension)) {
                drawOutline(targetPos, true);
            }
        } else if (!player.isShiftKeyDown()){
            BlockPos viewPos = getViewPos(world, player);
            if (viewPos != null) {
                drawOutline(viewPos, false);
            }
        }
    }

    public BlockPos getViewPos(Level world, Player player) {
        BlockPos hit = null;

        BlockHitResult trace = RaycastHelper.rayTraceRange(world, player, player.getBlockReach());
        if (trace != null && trace.getType() == HitResult.Type.BLOCK) {

            hit = trace.getBlockPos();
            boolean replaceable = world.getBlockState(hit).canBeReplaced(
                    new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, trace))
            );
            if (trace.getDirection().getAxis().isVertical() && !replaceable)
                hit = hit.relative(trace.getDirection());
        }
        return hit;
    }

    public void drawOutline(BlockPos selection, boolean selected) {
        if (selection == null)
            return;

        AABB boundingBox = new AABB(selection);
        CreateClient.OUTLINER.chaseAABB(outlineSlot, boundingBox)
                .colored(selected ? 0x9ede73 : 0x6886c5)
                .lineWidth(1 / 16f);
    }
}
