package net.apertyotis.createandesiteabound.content.schematic.wrapper;

import com.mojang.datafixers.util.Either;
import net.apertyotis.createandesiteabound.AllItems;
import net.apertyotis.createandesiteabound.content.schematic.deploy.SimpleSchematicItem;
import net.apertyotis.createandesiteabound.foundation.ClientEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static net.apertyotis.createandesiteabound.CreateAndesiteAbound.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class SimpleWrapperTooltipHandler {

    public static boolean active;
    public static boolean opened;
    public static ItemStack trackingStack = ItemStack.EMPTY;
    public static long lastTooltipTick;
    public static long lastTooltipTime;

    public static void tick() {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            active = false;
            return;
        }
        active = true;
        if (trackingStack.isEmpty())
            return;
        if (level.getGameTime() - lastTooltipTick > 5) {
            reset();
        }
    }

    public static boolean mouseScrolled(double delta) {
        if (!active || !opened || !ChecklistPageManager.isOpen() ||
            System.currentTimeMillis() - lastTooltipTime > 250)
            return false;
        if (delta < 0) {
            ChecklistPageManager.nextPage();
        } else if (delta > 0) {
            ChecklistPageManager.prevPage();
        }
        return true;
    }

    public static boolean mouseClick(int button, double ignore1, double ignore2) {
        if (!active || opened || button != ClientEvents.getKeyUseCode() ||
            System.currentTimeMillis() - lastTooltipTime > 250)
            return false;

        if (AllItems.SIMPLE_WRAPPER.isIn(trackingStack)) {
            opened = true;
            return true;
        }
        return false;
    }

    public static void tooltipTick() {
        Level level = Minecraft.getInstance().level;
        if (level == null)
            return;
        long current = level.getGameTime();
        if (lastTooltipTick == current)
            return;
        lastTooltipTick = current;
        lastTooltipTime = System.currentTimeMillis();
    }

    public static void reset() {
        opened = false;
        trackingStack = ItemStack.EMPTY;
        ChecklistPageManager.onClose();
    }

    public static boolean updateTracking(ItemStack stack) {
        if (!AllItems.SIMPLE_WRAPPER.isIn(stack)) {
            reset();
            return false;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("File", Tag.TAG_STRING) ||
            !tag.contains("Materials", Tag.TAG_BYTE_ARRAY)
        ) {
            reset();
            return false;
        }
        if (trackingStack != stack) {
            reset();
            trackingStack = stack;
        }
        return true;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void addToTooltip(ItemTooltipEvent event) {
        if (!active || Minecraft.getInstance().screen == null)
            return;
        ItemStack stack = event.getItemStack();
        if (!updateTracking(stack))
            return;
        tooltipTick();
        if (!opened) {
            List<Component> tooltip = event.getToolTip();
            String key = SimpleSchematicItem.getTranslateKey(stack);
            if (key == null)
                return;
            int i = Math.min(tooltip.size(), 1);
            tooltip.add(i, Component.translatable("item.createandesiteabound.simple_wrapper.tooltip1")
                    .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable(key).withStyle(ChatFormatting.GOLD)));
            Component keyUse = Minecraft.getInstance().options.keyUse.getKey().getDisplayName().copy()
                .withStyle(ChatFormatting.GRAY);
            tooltip.add(i + 1, Component.translatable("item.createandesiteabound.simple_wrapper.tooltip2", keyUse)
                .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void showRichNote(RenderTooltipEvent.GatherComponents event) {
        if (!active || Minecraft.getInstance().screen == null)
            return;
        ItemStack stack = event.getItemStack();
        if (!opened || stack.isEmpty() || stack != trackingStack)
            return;

        List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements();
        tooltip.clear();
        tooltip.add(Either.right(new ChecklistTooltipComponent()));
    }

}
