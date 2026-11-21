package net.apertyotis.createandesiteabound.content.schematic.wrapper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItem;
import com.simibubi.create.foundation.item.CustomUseEffectsItem;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.simibubi.create.foundation.mixin.accessor.LivingEntityAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SimpleWrapperItem extends Item implements CustomUseEffectsItem {

    public SimpleWrapperItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack item = player.getMainHandItem();
        ItemStack otherItem = player.getOffhandItem();
        if (hand == InteractionHand.MAIN_HAND && AllItems.SCHEMATIC.isIn(otherItem)) {
            player.startUsingItem(hand);
            item.getOrCreateTag().put("Polishing", otherItem.save(new CompoundTag()));
            otherItem.shrink(1);
            return InteractionResultHolder.consume(item);
        }
        return InteractionResultHolder.pass(item);
    }

    public void placeBackPolishing(ItemStack stack, Level world, LivingEntity entity) {
        CompoundTag tag = stack.getTag();
        if (tag == null)
            return;
        ItemStack polishing = ItemStack.of(tag.getCompound("Polishing"));
        tag.remove("Polishing");
        if (!polishing.isEmpty()) {
            ItemStack inHand = entity.getOffhandItem();
            if (inHand.isEmpty()) {
                entity.setItemInHand(InteractionHand.OFF_HAND, polishing);
            } else if (ItemHelper.canItemStackAmountsStack(polishing, inHand)) {
                inHand.grow(1);
            } else if (entity instanceof Player player) {
                player.getInventory().placeItemBackInInventory(polishing, false);
            } else {
                ItemEntity drop = new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), polishing);
                world.addFreshEntity(drop);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int timeCharged) {
        placeBackPolishing(stack, world, entity);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        placeBackPolishing(stack, world, entity);
        if (!(entity instanceof Player player))
            return stack;
        if (world.isClientSide) {
            SandPaperItem.spawnParticles(player.getEyePosition(1)
                .add(player.getLookAngle().scale(.5f)), AllItems.SCHEMATIC.asStack(), world);
            return stack;
        }

        CompoundTag tag = stack.getTag();
        ItemStack otherItem = entity.getOffhandItem();
        if (tag != null && tag.contains("File", Tag.TAG_STRING) && tag.contains("Materials", Tag.TAG_BYTE_ARRAY)) {
            SimpleWrapperChecklist checklist = SimpleWrapperChecklist.deserialize((ByteArrayTag) tag.get("Materials"));
            if (checklist == null) {
                player.displayClientMessage(Component.translatable("caa.schematic.error.invalid")
                    .withStyle(ChatFormatting.RED), true);
                return stack;
            }

            gatherItems(checklist, player);
            ItemStack result = createUsingResult(checklist, tag.getString("File"), player);
            if (!result.isEmpty()) {
                stack.shrink(1);
                player.getInventory().placeItemBackInInventory(result);
            }
        } else if (AllItems.SCHEMATIC.isIn(otherItem)) {
            ItemStack blueprint = otherItem.copy();
            blueprint.getOrCreateTag().putBoolean("Deployed", true);
            SimpleWrapperChecklist checklist = SimpleWrapperChecklist.create(world, blueprint);
            if (checklist == null) {
                player.displayClientMessage(Component.translatable("caa.schematic.error.invalid")
                    .withStyle(ChatFormatting.RED), true);
                return stack;
            }

            gatherItems(checklist, player);
            // noinspection DataFlowIssue
            ItemStack result = createUsingResult(checklist, blueprint.getTag().getString("File"), player);
            if (!result.isEmpty()) {
                stack.shrink(1);
                player.getInventory().placeItemBackInInventory(result);
            }
        }
        return stack;
    }

    public static void gatherItems(SimpleWrapperChecklist checklist, Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack inSlot = inventory.getItem(i);
            ItemStack result = checklist.countAndConsume(player, inSlot);
            if (result.getCount() != inSlot.getCount() || !ItemStack.isSameItemSameTags(result, inSlot)) {
                inventory.setItem(i, result);
            }
        }
    }

    public static ItemStack createUsingResult(SimpleWrapperChecklist checklist, String filename, Player player) {
        ItemStack result;
        if (checklist.isComplete()) {
            result = net.apertyotis.createandesiteabound.AllItems.SIMPLE_SCHEMATIC.asStack();
            CompoundTag tag = result.getOrCreateTag();
            tag.putBoolean("FromCreate", true);
            tag.putString("File", filename);
        } else {
            result = net.apertyotis.createandesiteabound.AllItems.SIMPLE_WRAPPER.asStack();
            CompoundTag tag = result.getOrCreateTag();
            tag.putString("File", filename);
            ByteArrayTag bytes = checklist.serialize();
            if (bytes == null) {
                player.displayClientMessage(Component.literal("IO Error"), true);
                return ItemStack.EMPTY;
            } else {
                tag.put("Materials", bytes);
            }
            tag.putFloat("Progress", checklist.getProgress());
        }
        return result;
    }

    @Override
    public Boolean shouldTriggerUseEffects(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean triggerUseEffects(ItemStack stack, LivingEntity entity, int count, RandomSource random) {
        ((LivingEntityAccessor) entity).create$callSpawnItemParticles(AllItems.SCHEMATIC.asStack(), 1);

        // After 6 ticks play the sound every 7th
        if ((entity.getTicksUsingItem() - 6) % 7 == 0)
            entity.playSound(entity.getEatingSound(stack), 0.9F + 0.2F * random.nextFloat(),
                random.nextFloat() * 0.2F + 0.9F);

        return true;
    }

    @Override
    public SoundEvent getEatingSound() {
        return AllSoundEvents.SANDING_SHORT.getMainEvent();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 20;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new SimpleWrapperItemRenderer()));
    }

    public static class SimpleWrapperDecorator implements IItemDecorator {
        @Override
        public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
            CompoundTag tag = stack.getTag();
            if (tag == null || !tag.contains("Progress", Tag.TAG_FLOAT))
                return false;

            int x = xOffset + 1;
            int y = yOffset + 13;
            int width = Math.round(14f * tag.getFloat("Progress"));
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(x, y, 199.9f);
            guiGraphics.fill(0, 0, 14, 2, 0xFF000000);
            guiGraphics.fill(0, 0, width, 1, 0xFF00FF00);
            pose.popPose();
            return true;
        }
    }
}
