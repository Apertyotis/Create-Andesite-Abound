package net.apertyotis.createandesiteabound.content.note;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RichNoteData implements RichNotePageProvider {
    List<Block> blocks = new ArrayList<>();

    public transient String sha256;
    public transient List<RichNotePage.ContentPage> pages;
    public transient int index = 0;

    @Override
    public void onOpen() {
        index = 0;
    }

    @Override
    public void initPages(Font font, int maxWidth, int maxHeight) {
        if (pages == null) {
            pages = new ArrayList<>();
            int currentHeight = 0;
            RichNotePage.ContentPage currentPage = null;
            List<FormattedCharSequence> remainingText = null;
            ImageData remainingImage = null;
            Iterator<Block> it = blocks.iterator();
            Block block = null;
            while (true) {
                if (remainingText != null) {
                    currentPage = new RichNotePage.ContentPage();
                    int lines = Math.min(maxHeight / font.lineHeight, remainingText.size());
                    for (FormattedCharSequence sequence: remainingText.subList(0, lines))
                        currentPage.content.add(Either.left(sequence));
                    if (lines < remainingText.size()) {
                        remainingText = remainingText.subList(lines, remainingText.size());
                        pages.add(currentPage);
                        currentPage = null;
                        continue;
                    } else {
                        currentPage.content.add(Either.left(FormattedCharSequence.EMPTY));
                        currentHeight = lines * font.lineHeight + 6;
                        remainingText = null;
                    }
                } else if (remainingImage != null) {
                    currentPage = new RichNotePage.ContentPage();
                    currentPage.content.add(Either.right(remainingImage));
                    currentHeight += block.cachedHeight;
                    if (currentHeight + 6 <= maxHeight) {
                        currentPage.content.add(Either.left(FormattedCharSequence.EMPTY));
                        currentHeight += 6;
                    }
                    remainingImage = null;
                }
                if (!it.hasNext()) {
                    if (currentPage != null)
                        pages.add(currentPage);
                    break;
                }
                block = it.next();
                int height = block.getHeight(font, maxWidth, maxHeight);
                if (height <= 0)
                    continue;
                if (currentPage == null)
                    currentPage = new RichNotePage.ContentPage();
                if (currentHeight + height > maxHeight) {
                    switch (block.getType()) {
                        case TEXT -> {
                            int lines = Math.max(0, (maxHeight - currentHeight) / font.lineHeight);
                            for (FormattedCharSequence sequence: block.textSplit.subList(0, lines))
                                currentPage.content.add(Either.left(sequence));
                            if (lines < block.textSplit.size())
                                remainingText = block.textSplit.subList(lines, block.textSplit.size());
                        }
                        case IMAGE -> remainingImage = block.image;
                    }
                    pages.add(currentPage);
                    currentHeight = 0;
                    currentPage = null;
                } else {
                    switch (block.getType()) {
                        case TEXT -> {
                            for (FormattedCharSequence sequence: block.textSplit)
                                currentPage.content.add(Either.left(sequence));
                            currentPage.content.add(Either.left(FormattedCharSequence.EMPTY));
                        }
                        case IMAGE -> {
                            currentPage.content.add(Either.right(block.image));
                            if (currentHeight + height + 6 <= maxHeight) {
                                currentPage.content.add(Either.left(FormattedCharSequence.EMPTY));
                                currentHeight += 6;
                            }
                        }
                    }
                    currentHeight += height;
                }
            }
        }
    }

    @Override
    public int getTotalPages() {
        return pages == null ? 0 : pages.size();
    }

    @Override
    public RichNotePage.ContentPage getCurrentPage() {
        if (pages == null || pages.isEmpty())
            return null;
        if (index < 0 || index >= pages.size())
            index = 0;
        return pages.get(index);
    }

    @Override
    public int getPageIndex() {
        return index;
    }

    @Override
    public void setPageIndex(int index) {
        this.index = index;
    }

    public static class Block {
        JsonObject text;
        ImageData image;

        transient BlockType type = BlockType.UNRESOLVED;
        transient MutableComponent resolvedText;
        transient List<FormattedCharSequence> textSplit;
        transient int cachedHeight = -1;

        @SuppressWarnings("removal")
        public BlockType getType() {
            if (type == BlockType.UNRESOLVED) {
                try {
                    if (text != null) {
                        resolvedText = Component.Serializer.fromJson(text);
                        type = BlockType.TEXT;
                    } else if (image != null && image.texture != null && image.w > 0 && image.h > 0) {
                        if (!image.texture.endsWith(".png"))
                            image.texture += ".png";
                        image.location = new ResourceLocation(image.texture);
                        type = BlockType.IMAGE;
                    } else {
                        type = BlockType.INVALID;
                    }
                } catch (JsonSyntaxException | ResourceLocationException e) {
                    type = BlockType.INVALID;
                }
            }
            return type;
        }

        public int getHeight(Font font, int maxWidth, int maxHeight) {
            if (cachedHeight == -1) {
                switch (getType()) {
                    case UNRESOLVED, INVALID -> cachedHeight = 0;
                    case TEXT -> {
                        textSplit = font.split(resolvedText, maxWidth);
                        cachedHeight = textSplit.size() * font.lineHeight + 6;
                    }
                    case IMAGE -> {
                        double scaleX = (double) image.w / maxWidth;
                        double scaleY = (double) image.h / maxHeight;
                        double scale = Math.max(scaleX, scaleY);
                        if (scale <= 0) {
                            image.scale = 0;
                            cachedHeight = 0;
                        } else if (scale >= 1) {
                            image.scale = 1 / scale;
                            cachedHeight = (int) (image.h * image.scale);
                        } else {
                            image.scale = 1;
                            cachedHeight = image.h;
                        }
                    }
                }
            }
            return cachedHeight;
        }
    }

    public static class ImageData {
        public String texture;
        public int w;
        public int h;

        public transient ResourceLocation location;
        public transient double scale;
    }

    public enum BlockType {
        UNRESOLVED, INVALID, TEXT, IMAGE
    }
}