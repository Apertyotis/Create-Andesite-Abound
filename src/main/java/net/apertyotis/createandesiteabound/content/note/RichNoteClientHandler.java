package net.apertyotis.createandesiteabound.content.note;

import com.google.common.base.Strings;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import net.apertyotis.createandesiteabound.AllKey;
import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
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

@Mod.EventBusSubscriber(modid = CreateAndesiteAbound.MOD_ID, value = Dist.CLIENT)
public class RichNoteClientHandler {
    public static boolean active = false;
    public static boolean opened = false;
    public static long lastTooltipTick;
    public static ItemStack trackingStack = ItemStack.EMPTY;
    public static LerpedFloat progress = LerpedFloat.linear().startWithValue(0);

    public static void tick() {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            active = false;
            return;
        }
        active = true;
        if (trackingStack.isEmpty())
            return;
        if (level.getGameTime() - lastTooltipTick > 1) {
            reset();
        }
    }

    public static boolean mouseScrolled(double delta) {
        if (!active || !opened || RichNoteDataManager.cachedData == null)
            return false;
        if (delta < 0) {
            RichNoteDataManager.cachedData.nextPage();
        } else if (delta > 0) {
            RichNoteDataManager.cachedData.prevPage();
        }
        return true;
    }

    public static void reset() {
        opened = false;
        trackingStack = ItemStack.EMPTY;
        progress.startWithValue(0);
    }

    public static void deferredTick() {
        Level level = Minecraft.getInstance().level;
        if (level == null)
            return;
        long current = level.getGameTime();
        if (lastTooltipTick == current)
            return;
        lastTooltipTick = current;
        if (opened)
            return;
        float value = progress.getValue();
        if (value >= 1) {
            opened = true;
            RichNoteDataManager.cachedData = RichNoteDataManager.get(trackingStack);
            return;
        }
        int keyCode = AllKey.NOTE_KEY.getKey().getValue();
        long window = Minecraft.getInstance().getWindow().getWindow();
        if (InputConstants.isKeyDown(window, keyCode))
            progress.setValue(Math.min(1, value + Math.max(.25f, value) * .25f));
        else
            progress.setValue(Math.max(0, value - .05));
    }

    public static boolean updateTracking(ItemStack stack) {
        if (RichNoteDataManager.get(stack) == null) {
            reset();
            return false;
        }
        if (!trackingStack.is(stack.getItem())) {
            reset();
        }
        trackingStack = stack;
        return !stack.isEmpty();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void addToTooltip(ItemTooltipEvent event) {
        if (!active || Minecraft.getInstance().screen == null)
            return;
        if (!updateTracking(event.getItemStack()))
            return;
        deferredTick();
        List<Component> tooltip = event.getToolTip();
        if (!opened) {
            float partialTicks = Minecraft.getInstance().getFrameTime();
            Component component = makeProgressBar(Math.min(1, progress.getValue(partialTicks) * 8 / 7f));
            if (tooltip.size() < 2) {
                tooltip.add(component);
            } else {
                tooltip.add(1, component);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void showRichNote(RenderTooltipEvent.GatherComponents event) {
        if (!active || Minecraft.getInstance().screen == null)
            return;
        ItemStack stack = event.getItemStack();
        if (!opened || stack.isEmpty() || !trackingStack.is(stack.getItem()))
            return;

        List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements();
        tooltip.clear();
        tooltip.add(Either.right(new RichNoteTooltipComponent()));
    }

    private static Component makeProgressBar(float progress) {
        MutableComponent keyCtrl = ((MutableComponent) AllKey.NOTE_KEY.getTranslatedKeyMessage())
            .withStyle(ChatFormatting.GRAY);
        MutableComponent holdCtrl = Component.translatable("caa.note.hold_open", keyCtrl)
            .withStyle(ChatFormatting.DARK_GRAY);

        Font font = Minecraft.getInstance().font;
        float charWidth = font.width("|");
        float tipWidth = font.width(holdCtrl);

        int total = (int) (tipWidth / charWidth);
        int current = (int) (progress * total);

        if (progress > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(ChatFormatting.GRAY)
                .append(Strings.repeat("|", current));
            if (progress < 1) {
                builder.append(ChatFormatting.DARK_GRAY)
                    .append(Strings.repeat("|", total - current));
            }
            return Components.literal(builder.toString());
        } else {
            return holdCtrl.append(Component.literal(" ★").withStyle(ChatFormatting.YELLOW));
        }
    }
}
