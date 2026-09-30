package net.apertyotis.createandesiteabound.content.note;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public abstract class RichNotePage {

    public abstract void render(Font font, int x, int y, GuiGraphics guiGraphics);

    public static class ContentPage extends RichNotePage {
        public List<Either<FormattedCharSequence, RichNoteData.ImageData>> content = new ArrayList<>();

        @Override
        public void render(Font font, int x, int y, GuiGraphics guiGraphics) {
            x += RichNoteTooltipComponent.LEFT_PADDING;
            y += RichNoteTooltipComponent.TOP_PADDING;
            for (Either<FormattedCharSequence, RichNoteData.ImageData> line: content) {
                if (line.left().isPresent()) {
                    FormattedCharSequence sequence = line.left().get();
                    if (sequence == FormattedCharSequence.EMPTY) {
                        y += 6;
                    } else {
                        guiGraphics.drawString(font, sequence, x, y, 0x311A00, false);
                        y += font.lineHeight;
                    }
                } else if (line.right().isPresent()) {
                    RichNoteData.ImageData image = line.right().get();
                    int xScaled = (int) (image.w * image.scale);
                    int yScaled = (int) (image.h * image.scale);
                    int xOffset = (RichNoteTooltipComponent.CONTENT_WIDTH - xScaled) / 2;
                    if (xOffset <= 1)
                        xOffset = 0;
                    PoseStack ms = guiGraphics.pose();
                    ms.pushPose();
                    ms.translate(x + xOffset, y, 0);
                    ms.scale((float) image.scale, (float) image.scale, 1);
                    guiGraphics.blit(image.location, 0, 0, 0, 0, image.w, image.h, image.w, image.h);
                    ms.popPose();
                    y += yScaled;
                }
            }
        }
    }

    public static class IndexPage extends RichNotePage {

        public int itemIndex = 0;
        public List<ResourceLocation> items = new ArrayList<>();

        public static final int LINE_HEIGHT = 24;

        @Override
        public void render(Font font, int x, int y, GuiGraphics guiGraphics) {
            for (int i = 0; i < items.size(); i++) {
                ResourceLocation id = items.get(i);
                Item item = ForgeRegistries.ITEMS.getValue(id);
                if (item == null || item == Items.AIR)
                    continue;
                Component name = item.getDescription();
                boolean checked = !RichNoteReadRecord.isUnread(item);

                guiGraphics.drawString(font, "□",
                    x + RichNoteTooltipComponent.LEFT_PADDING, y + RichNoteTooltipComponent.TOP_PADDING + 5,
                    checked ? 0x668D7F6B : 0xff8D7F6B, false);
                if (checked)
                    guiGraphics.drawString(font, "✔",
                        x + RichNoteTooltipComponent.LEFT_PADDING, y + RichNoteTooltipComponent.TOP_PADDING + 4,
                        0x31B25D, false);

                guiGraphics.renderItem(item.getDefaultInstance(),
                    x + RichNoteTooltipComponent.LEFT_PADDING + 9, y + RichNoteTooltipComponent.TOP_PADDING);

                guiGraphics.drawString(font, name,
                    x + RichNoteTooltipComponent.LEFT_PADDING + 29, y + RichNoteTooltipComponent.TOP_PADDING + 4,
                    checked ? 0x31B25D : 0x311A00, false);

                if (i == itemIndex) {
                    int width = font.width(name);
                    guiGraphics.drawString(font, "<=",
                        x + RichNoteTooltipComponent.LEFT_PADDING + width + 32, y + RichNoteTooltipComponent.TOP_PADDING + 4,
                        0x311A00, false);
                }

                y += LINE_HEIGHT;
            }
        }
    }
}
