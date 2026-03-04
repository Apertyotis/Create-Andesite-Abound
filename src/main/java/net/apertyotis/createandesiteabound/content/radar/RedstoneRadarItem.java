package net.apertyotis.createandesiteabound.content.radar;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RedstoneRadarItem extends BlockItem {
    public RedstoneRadarItem(Block p_40565_, Properties p_40566_) {
        super(p_40565_, p_40566_);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack heldItem = context.getItemInHand();
        if (player == null)
            return InteractionResult.PASS;

        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        CompoundTag tag = heldItem.getOrCreateTag();

        if (player.isShiftKeyDown()) {
            heldItem.setTag(null);
            player.displayClientMessage(Component.translatable("msg.caa.target_reset")
                    .withStyle(ChatFormatting.GOLD), true);
        } else {
            if (tag.contains("TargetPos") && tag.contains("TargetDimension")) {
                if (world.isClientSide())
                    return InteractionResult.SUCCESS;
                ((BlockItem) heldItem.getItem()).place(new BlockPlaceContext(context));
            } else {
                BlockPos targetPos = pos;
                Direction side = context.getClickedFace();
                boolean replaceable = world.getBlockState(targetPos).canBeReplaced(new BlockPlaceContext(context));

                if (side.getAxis().isVertical() && !replaceable)
                    targetPos = targetPos.relative(side);

                String targetDimension = world.dimensionTypeId().location().toString();

                tag.put("TargetPos", NbtUtils.writeBlockPos(targetPos));
                tag.putString("TargetDimension", targetDimension);
                heldItem.setTag(tag);
                player.displayClientMessage(Component.translatable("msg.caa.target_set")
                        .withStyle(ChatFormatting.GOLD), true);
                player.getCooldowns().addCooldown(this, 5);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains("TargetPos") || !tag.contains("TargetDimension"))
            return;

        BlockPos targetPos = NbtUtils.readBlockPos(tag.getCompound("TargetPos"));
        ResourceLocation id = new ResourceLocation(tag.getString("TargetDimension"));
        String dimensionDescId = "dimension." + id.getNamespace() + "." + id.getPath();

        tooltip.add(Component.translatable("tooltip.caa.has_target_nbt")
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("  ")
                .append(Component.translatable(dimensionDescId)));
        tooltip.add(Component.literal("  ")
                .append(targetPos.toShortString()));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("TargetPos") && tag.contains("TargetDimension");
    }
}
