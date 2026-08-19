package net.apertyotis.createandesiteabound.content.filtering;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.equipment.toolbox.*;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.apertyotis.createandesiteabound.AllPackets;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RadialFilteringMenu extends AbstractSimiScreen {

    private int ticksOpen;
    private int hoveredSlot = -1;
    private boolean scrollMode;
    private int scrollSlot;

    private final List<ItemStack> filters;
    private final BlockPos heldPos;
    private final Direction heldSide;

    public RadialFilteringMenu(List<ItemStack> filters, BlockPos pos, Direction side) {
        this.filters = filters;
        this.heldPos = pos;
        this.heldSide = side;
    }

    @Override
    public void tick() {
        ticksOpen++;
        super.tick();
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        int a = ((int) (0x50 * Math.min(1, (ticksOpen + AnimationTickHolder.getPartialTicks()) / 20f))) << 24;
        graphics.fillGradient(0, 0, this.width, this.height, 0x101010 | a, 0x101010 | a);
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        float fade = Mth.clamp((ticksOpen + AnimationTickHolder.getPartialTicks()) / 10f, 1 / 512f, 1);

        hoveredSlot = -1;
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
        {
            for (int slot = 0; slot < 8; slot++) {
                ms.pushPose();
                TransformStack.cast(ms)
                    .rotateZ(slot * 45 - 45)
                    .translate(0, -40 + (10 * (1 - fade) * (1 - fade)), 0)
                    .rotateZ(-slot * 45 + 45);
                ms.translate(-12, -12, 0);

                if (slot < filters.size()) {
                    ItemStack stack = filters.get(slot);
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
        }
        ms.popPose();

        if (tip != null) {
            int i1 = Math.min((int) (fade * 255), 255);

            if (i1 > 8) {
                ms.pushPose();
                ms.translate((float) (width / 2), (float) (height - 68), 0.0F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                int k1 = 16777215;
                int k = i1 << 24 & -16777216;
                int l = font.width(tip);
                graphics.drawString(font, tip, Math.round(-l / 2f), -4, k1 | k, false);
                RenderSystem.disableBlend();
                ms.popPose();
            }
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int selected = scrollMode ? scrollSlot : hoveredSlot;
        if (button == 0 && selected >= 0 && selected < filters.size()) {
            onClose();
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        int selected = scrollMode ? scrollSlot : hoveredSlot;
        if (button == 1 && selected >= 0 && selected < filters.size()) {
            onClose();
            return true;
        }
        return super.mouseReleased(x, y, button);
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
                int size = filters.size();
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
                if (i < filters.size()) {
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

        int selected = (scrollMode ? scrollSlot : hoveredSlot);
        if (selected < 0 || selected >= filters.size())
            return;
        ItemStack stack = filters.get(selected);
        if (stack.isEmpty())
            return;

        AllPackets.getChannel().sendToServer(new EasyFilteringPacket(heldPos, heldSide, stack));
    }
}
