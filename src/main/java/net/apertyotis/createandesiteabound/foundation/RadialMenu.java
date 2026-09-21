package net.apertyotis.createandesiteabound.foundation;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.equipment.toolbox.*;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class RadialMenu extends AbstractSimiScreen {

    public static final int NONE = -1;
    public static final int LEFT = -2;
    public static final int RIGHT = -3;

    private boolean ignoreMouseRelease = false;
    private int ticksOpen;
    private int hoveredSlot = NONE;
    private boolean scrollMode;
    private int scrollSlot;
    private int page = 0;

    private final List<ItemStack> items;
    private final Consumer<Integer> callback;
    private List<String> tooltips;

    public RadialMenu(List<ItemStack> items, Consumer<Integer> callback) {
        this.items = items;
        this.callback = callback;
    }

    public void withTooltip(List<String> tooltips) {
        this.tooltips = tooltips;
    }

    @Override
    public void tick() {
        ticksOpen++;
        super.tick();
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        int a = ((int) (0x50 * Math.min(1, (ticksOpen + AnimationTickHolder.getPartialTicks()) / 10f))) << 24;
        graphics.fillGradient(0, 0, this.width, this.height, 0x101010 | a, 0x101010 | a);
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int ticks = Math.max(1, AllConfig.toolbelt_animation_ticks);
        float fade = (ticksOpen + AnimationTickHolder.getPartialTicks()) / ticks;
        fade = Mth.clamp(fade, 1 / 512f, 1);

        hoveredSlot = NONE;
        Window window = getMinecraft().getWindow();
        float hoveredX = mouseX - window.getGuiScaledWidth() / 2f;
        float hoveredY = mouseY - window.getGuiScaledHeight() / 2f;

        float distance = hoveredX * hoveredX + hoveredY * hoveredY;
        if (distance > 25 && distance < 10000)
            hoveredSlot =
                (Mth.floor((AngleHelper.deg(Mth.atan2(hoveredY, hoveredX)) + 360 + 180 - 22.5f)) % 360) / 45;
        if (scrollMode && distance > 150)
            scrollMode = false;

        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(width / 2f, height / 2f, 0);
        Component tip = null;

        float offset = 5 * (1 - fade) * (1 - fade);
        if (page > 0) {
            ms.pushPose();
            ms.translate(-80 + offset, 0, 0);
            AllGuiTextures.TOOLBELT_SLOT.render(graphics, -12, -12);
            ms.translate(-0.5, 0.5, 0);
            AllIcons.I_MTD_LEFT.render(graphics, -9, -9);
            ms.translate(0.5, -0.5, 0);
            if (hoveredX > -100 && hoveredX < -60 && hoveredY > -20 && hoveredY < 20) {
                hoveredSlot = LEFT;
                AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, -13, -13);
            }
            ms.popPose();
        }
        if (page + 1 < Math.ceil(items.size() / 8f)) {
            ms.pushPose();
            ms.translate(80 - offset, 0, 0);
            AllGuiTextures.TOOLBELT_SLOT.render(graphics, -12, -12);
            ms.translate(-0.5, 0.5, 0);
            AllIcons.I_MTD_RIGHT.render(graphics, -9, -9);
            ms.translate(0.5, -0.5, 0);
            if (hoveredX > 60 && hoveredX < 100 && hoveredY > -20 && hoveredY < 20) {
                hoveredSlot = RIGHT;
                AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, -13, -13);
            }
            ms.popPose();
        }

        for (int slot = 0; slot < 8; slot++) {
            int index = page * 8 + slot;
            ms.pushPose();
            TransformStack.cast(ms)
                .rotateZ(slot * 45 - 45)
                .translate(0, -40 + (10 * (1 - fade) * (1 - fade)), 0)
                .rotateZ(-slot * 45 + 45);
            ms.translate(-12, -12, 0);

            if (index >= 0 && index < items.size()) {
                ItemStack stack = items.get(index);
                AllGuiTextures.TOOLBELT_SLOT.render(graphics, 0, 0);
                GuiGameElement.of(stack)
                    .at(3, 3)
                    .render(graphics);

                if (slot == (scrollMode ? scrollSlot : hoveredSlot)) {
                    AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, -1, -1);
                    tip = stack.getHoverName();
                }
            } else {
                AllGuiTextures.TOOLBELT_EMPTY_SLOT.render(graphics, 0, 0);
            }

            ms.popPose();
        }
        ms.popPose();

        int i1 = Math.min((int) (fade * 255), 255);
        if (i1 > 8) {
            ms.pushPose();
            ms.translate((float) (width / 2), (float) (height - 86), 0.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int color = (i1 << 24 & 0xFF0000) | 0xFFFFFF;
            if (tip != null) {
                int l = font.width(tip);
                graphics.drawString(font, tip, Math.round(-l / 2f), -4, color, false);
            }
            if (tooltips != null) {
                ms.translate(0, font.lineHeight, 0);
                for (String tooltip: tooltips) {
                    ms.translate(0, font.lineHeight, 0);
                    int l = font.width(tooltip);
                    graphics.drawString(font, tooltip, Math.round(-l / 2f), -4, color, false);
                }
            }
            RenderSystem.disableBlend();
            ms.popPose();
        }
    }

    public boolean onMouseButton() {
        ignoreMouseRelease = true;
        int slot = scrollMode ? scrollSlot : hoveredSlot;
        if (slot == LEFT) {
            if (page > 0)
                page--;
            return true;
        } else if (slot == RIGHT) {
            if (page + 1 < Math.ceil(items.size() / 8f))
                page++;
            return true;
        }
        int selected = page * 8 + slot;
        if (selected >= 0 && selected < items.size()) {
            onClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (onMouseButton())
            return true;
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (ignoreMouseRelease)
            return super.mouseReleased(x, y, button);
        return onMouseButton();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Window window = Minecraft.getInstance().getWindow();
        double hoveredX = mouseX - window.getGuiScaledWidth() / 2f;
        double hoveredY = mouseY - window.getGuiScaledHeight() / 2f;
        double distance = hoveredX * hoveredX + hoveredY * hoveredY;
        if (distance <= 150) {
            if (!scrollMode) {
                scrollMode = true;
                scrollSlot = 0;
            } else {
                int size = items.size();
                scrollSlot = (scrollSlot - Mth.sign(delta) + size) % size;
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int code, int scanCode, int modifiers) {
        KeyMapping[] hotbarBinds = Minecraft.getInstance().options.keyHotbarSlots;
        for (int i = 0; i < hotbarBinds.length && i < 8; i++) {
            if (hotbarBinds[i].matches(code, scanCode)) {
                if (i < items.size()) {
                    scrollMode = true;
                    scrollSlot = i;
                    onClose();
                }
                return true;
            }
        }
        return super.keyPressed(code, scanCode, modifiers);
    }

    @Override
    public void removed() {
        super.removed();

        int slot = scrollMode ? scrollSlot : hoveredSlot;
        int selected = page * 8 + slot;
        if (slot < 0 || selected < 0 || selected >= items.size())
            selected = NONE;

        if (callback != null)
            callback.accept(selected);
    }
}
