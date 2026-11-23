package net.apertyotis.createandesiteabound.content.schematic.wrapper;

import com.mojang.datafixers.util.Either;
import com.simibubi.create.foundation.gui.ILightingSettings;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.utility.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChecklistPage {

    public static final int LEFT_PADDING = ChecklistPageManager.LEFT_PADDING;
    public static final int TOP_PADDING = ChecklistPageManager.TOP_PADDING;
    public static final int LINE_HEIGHT = ChecklistPageManager.LINE_HEIGHT;

    public final List<Map.Entry<Either<ItemStack, Fluid>, SimpleWrapperChecklist.CheckEntry>> entries = new ArrayList<>();

    public void render(Font font, int x, int y, GuiGraphics guiGraphics) {
        for (var entry : entries) {
            int finalY = y;
            MutableComponent text = Component.empty();
            boolean checked = entry.getValue().isComplete();
            entry.getKey().ifLeft(stack -> {
                text.append(stack.getDisplayName().copy().setStyle(Style.EMPTY));
                if (checked) {
                    text.withStyle(ChatFormatting.DARK_GREEN);
                } else {
                    int amount = entry.getValue().total - entry.getValue().count;
                    int stacks = amount / 64;
                    int remainder = amount % 64;
                    if (stacks > 0) {
                        text.append(Components.literal(" " + stacks + "▤+" + remainder)
                            .withStyle(ChatFormatting.BLACK));
                    } else {
                        text.append(Components.literal(" x" + amount)
                            .withStyle(ChatFormatting.BLACK));
                    }
                }
                guiGraphics.renderItem(stack, x + LEFT_PADDING + 9, finalY + TOP_PADDING);
            });
            entry.getKey().ifRight(fluid -> {
                text.append("[");
                text.append(fluid.getFluidType().getDescription().copy().setStyle(Style.EMPTY));
                text.append("]");
                if (checked) {
                    text.withStyle(ChatFormatting.DARK_GREEN);
                } else {
                    int buckets = entry.getValue().total - entry.getValue().count;
                    text.append(Components.literal(" x" + buckets + "B")
                        .withStyle(ChatFormatting.BLACK));
                }
                GuiGameElement.of(fluid)
                    .<GuiGameElement.GuiRenderBuilder>at(x + LEFT_PADDING + 9, finalY + TOP_PADDING + 16, 100)
                    .lighting(ILightingSettings.DEFAULT_FLAT)
                    .scale(16)
                    .render(guiGraphics);
            });

            guiGraphics.drawString(font, "□", x + LEFT_PADDING, y + TOP_PADDING + 5,
                checked ? 0x668D7F6B : 0xff8D7F6B, false);
            if (checked)
                guiGraphics.drawString(font, "✔", x + LEFT_PADDING, y + TOP_PADDING + 4,
                    0x31B25D, false);
            guiGraphics.drawString(font, text, x + LEFT_PADDING + 29, y + TOP_PADDING + 4,
                0x311A00, false);
            y += LINE_HEIGHT;
        }
    }
}
