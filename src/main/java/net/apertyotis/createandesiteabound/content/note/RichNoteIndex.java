package net.apertyotis.createandesiteabound.content.note;

import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class RichNoteIndex implements RichNotePageProvider {
    public int pageIndex;
    public int itemIndex;
    public List<RichNotePage.IndexPage> pages;

    public ResourceLocation getSelection() {
        RichNotePage.IndexPage page = (RichNotePage.IndexPage) getCurrentPage();
        if (page == null)
            return null;
        if (itemIndex < 0 || itemIndex >= page.items.size()) {
            itemIndex = 0;
            page.itemIndex = 0;
        }
        return page.items.get(itemIndex);
    }

    @Override
    public void initPages(Font font, int maxWidth, int maxHeight) {
        if (pages == null) {
            List<ResourceLocation> items = RichNoteDataManager.getKeys().stream().sorted().toList();
            if (items.isEmpty())
                return;
            int lines = maxHeight / RichNotePage.IndexPage.LINE_HEIGHT;
            if (lines <= 0)
                return;

            pages = new ArrayList<>();
            for (int i = 0; i < items.size();) {
                RichNotePage.IndexPage page = new RichNotePage.IndexPage();
                page.items.addAll(items.subList(i, Math.min(items.size(), i + lines)));
                pages.add(page);
                i += lines;
            }
        }
    }

    @Override
    public RichNotePage getCurrentPage() {
        if (pages == null || pages.isEmpty())
            return null;
        if (pageIndex < 0 || pageIndex >= pages.size())
            pageIndex = 0;
        RichNotePage.IndexPage page = pages.get(pageIndex);
        page.itemIndex = itemIndex;
        return page;
    }

    @Override
    public int getTotalPages() {
        return pages == null ? 0 : pages.size();
    }

    @Override
    public int getPageIndex() {
        return pageIndex;
    }

    @Override
    public void setPageIndex(int index) {
        if (pageIndex != index) {
            pageIndex = index;
            itemIndex = 0;
        }
    }

    @Override
    public void onClose() {
        pageIndex = 0;
        itemIndex = 0;
    }

    @Override
    public void nextPage() {
        if (RichNoteClientHandler.isNoteKeyDown()) {
            RichNotePageProvider.super.nextPage();
        } else {
            RichNotePage.IndexPage page = (RichNotePage.IndexPage) getCurrentPage();
            if (page == null)
                return;
            if (itemIndex < page.items.size() - 1) {
                itemIndex++;
                page.itemIndex = itemIndex;
            }
        }
    }

    @Override
    public void prevPage() {
        if (RichNoteClientHandler.isNoteKeyDown()) {
            RichNotePageProvider.super.prevPage();
        } else {
            RichNotePage.IndexPage page = (RichNotePage.IndexPage) getCurrentPage();
            if (page == null)
                return;
            if (itemIndex > 0) {
                itemIndex--;
                page.itemIndex = itemIndex;
            }
        }
    }
}
