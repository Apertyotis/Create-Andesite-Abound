package net.apertyotis.createandesiteabound.content.note;

import net.apertyotis.createandesiteabound.foundation.ClientEvents;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class RichNotePageManager {
    public static RichNoteIndex cachedIndexPage;
    public static RichNotePageProvider currentPage;

    public static void onReload() {
        onClose();
        cachedIndexPage = null;
    }

    public static void onClose() {
        if (cachedIndexPage != null)
            cachedIndexPage.onClose();
        if (currentPage != cachedIndexPage && currentPage != null)
            currentPage.onClose();
        currentPage = null;
    }

    public static void open(Item item) {
        currentPage = RichNoteDataManager.get(item);
        if (currentPage != null) {
            currentPage.onOpen();
            RichNoteReadRecord.markRead(item);
        }
    }

    public static void open(ResourceLocation key) {
        currentPage = RichNoteDataManager.get(key);
        if (currentPage != null) {
            currentPage.onOpen();
            RichNoteReadRecord.markRead(key);
        }
    }

    public static void openTitle() {
        if (cachedIndexPage == null) {
            cachedIndexPage = new RichNoteIndex();
        }
        currentPage = cachedIndexPage;
    }

    public static boolean isOpen() {
        return currentPage != null;
    }

    public static boolean mouseClick(int button, double ignored1, double ignored2) {
        if (currentPage == null)
            return false;
        if (button == ClientEvents.getKeyAttackCode()) {
            if (currentPage instanceof RichNoteIndex) {
                ResourceLocation key = ((RichNoteIndex) currentPage).getSelection();
                open(key);
            }
        } else if (button == ClientEvents.getKeyUseCode()) {
            if (currentPage instanceof RichNoteData) {
                openTitle();
            }
        } else {
            return false;
        }
        return true;
    }

    public static RichNotePage resolveCurrentPage(Font font, int maxWidth, int maxHeight) {
        if (currentPage == null)
            return null;
        currentPage.initPages(font, maxWidth, maxHeight);
        return currentPage.getCurrentPage();
    }

    public static void nextPage() {
        currentPage.nextPage();
    }

    public static void prevPage() {
        currentPage.prevPage();
    }

    public static int getTotalPages() {
        return currentPage.getTotalPages();
    }

    public static int getIndex() {
        return currentPage.getPageIndex();
    }
}
