package net.apertyotis.createandesiteabound.content.note;

import net.minecraft.client.gui.Font;

public interface RichNotePageProvider {
    void initPages(Font font, int maxWidth, int maxHeight);
    RichNotePage getCurrentPage();
    int getTotalPages();
    int getPageIndex();
    void setPageIndex(int index);

    default void onOpen() {}
    default void onClose() {}

    default void nextPage() {
        int index = getPageIndex();
        if (index < getTotalPages() - 1)
            setPageIndex(index + 1);
    }

    default void prevPage() {
        int index = getPageIndex();
        if (index > 0)
            setPageIndex(index - 1);
    }
}
