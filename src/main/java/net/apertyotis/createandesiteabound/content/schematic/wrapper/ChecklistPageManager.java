package net.apertyotis.createandesiteabound.content.schematic.wrapper;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChecklistPageManager {

    public static final int CONTENT_HEIGHT = 180;
    public static final int LEFT_PADDING = 33;
    public static final int TOP_PADDING = 50;
    public static final int CONTENT_WIDTH = 162;
    public static final int LINE_HEIGHT = 24;

    public static final List<ChecklistPage> pages = new ArrayList<>();
    public static boolean opened = false;
    public static int index = 0;

    public static void initPages(int maxHeight) {
        pages.clear();
        opened = true;
        index = 0;

        ItemStack stack = SimpleWrapperTooltipHandler.trackingStack;
        SimpleWrapperChecklist checklist = SimpleWrapperChecklist.deserialize(stack);
        if (checklist == null)
            return;
        var list = checklist.getList();
        if (list == null || list.isEmpty())
            return;

        int lines = maxHeight / LINE_HEIGHT;
        if (lines <= 0)
            return;

        for (int i = 0; i < list.size(); i += lines) {
            ChecklistPage page = new ChecklistPage();
            page.entries.addAll(list.subList(i, Math.min(list.size(), i + lines)));
            pages.add(page);
        }
    }

    public static ChecklistPage resolveCurrentPage(int maxHeight) {
        if (!opened) {
            initPages(maxHeight);
        }
        if (index < 0 || index >= pages.size()) {
            onClose();
            return null;
        }
        return pages.get(index);
    }

    public static void onClose() {
        pages.clear();
        opened = false;
        index = 0;
    }

    public static boolean isOpen() {
        return opened;
    }

    public static void nextPage() {
        index = Math.min(index + 1, pages.size() - 1);
    }

    public static void prevPage() {
        index = Math.max(index - 1, 0);
    }

    public static void render(Font font, int x, int y, GuiGraphics guiGraphics) {
        ChecklistPage page = resolveCurrentPage(CONTENT_HEIGHT);
        if (page == null)
            return;
        page.render(font, x, y, guiGraphics);
        int current = index + 1;
        int total = pages.size();
        String pageIndicator = "%d / %d".formatted(current, total);
        int width = font.width(pageIndicator);
        int xOffset = (CONTENT_WIDTH - width) / 2;
        guiGraphics.drawString(font, pageIndicator,
            x + LEFT_PADDING + xOffset, y + TOP_PADDING + CONTENT_HEIGHT + 3,
            0x000000, false);
    }
}
