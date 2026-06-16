package net.apertyotis.createandesiteabound.mixin.create.redstone.thresholdSwitch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchScreen;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.AbstractSimiWidget;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import net.apertyotis.createandesiteabound.AllPackets;
import net.apertyotis.createandesiteabound.content.thresholdSwitch.ChangeModeButton;
import net.apertyotis.createandesiteabound.content.thresholdSwitch.ConfigurePreciseThresholdSwitchPacket;
import net.apertyotis.createandesiteabound.content.thresholdSwitch.ThresholdSwitchBlockEntityEx;
import net.apertyotis.createandesiteabound.content.thresholdSwitch.ThresholdSwitchBlockEntityEx.ThresholdType;
import net.apertyotis.createandesiteabound.content.thresholdSwitch.ThresholdSwitchScreenBackground;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ThresholdSwitchScreen.class, remap = false)
public abstract class ThresholdSwitchScreenMixin extends AbstractSimiScreen {
    @Shadow
    private AllGuiTextures background;
    @Shadow
    @Final
    private ItemStack renderedItem;
    @Shadow
    private ThresholdSwitchBlockEntity blockEntity;
    @Shadow
    private ScrollInput onAbove;
    @Shadow
    private ScrollInput offBelow;
    @Shadow
    private int lastModification;

    @Unique
    private LerpedFloat caa$cursor;
    @Unique
    private boolean caa$precision = false;
    @Unique
    private ScrollInput caa$offBelow;
    @Unique
    private ScrollInput caa$onAbove;
    @Unique
    private SelectionScrollInput caa$inStacksOrBuckets;

    @Inject(method = "init", at = @At("TAIL"), remap = true)
    private void initEx(CallbackInfo ci) {
        int x = guiLeft;
        int y = guiTop;

        ThresholdSwitchBlockEntityEx ex = (ThresholdSwitchBlockEntityEx) blockEntity;

        boolean highlightTopRow = blockEntity.isInverted() ^ blockEntity.isPowered();
        caa$cursor = LerpedFloat.linear().startWithValue(highlightTopRow ? 0 : 1);

        ChangeModeButton changeModeButton =
            new ChangeModeButton(x + background.width - 85, y + background.height - 24, AllIcons.I_TARGET)
                .withCallback(() -> caa$setMode(!caa$precision));
        changeModeButton.setToolTip(Component.translatable("caa.gui.threshold.precision_mode"));
        changeModeButton.down = ex.caa$isPrecision();

        List<Component> selections = ex.caa$getTypeOfCurrentTarget() == ThresholdType.ITEM ?
            List.of(Component.translatable("caa.schedule.threshold.items"),
                Component.translatable("caa.schedule.threshold.stacks")) :
            List.of(Component.translatable("caa.schedule.threshold.milibuckets"),
                Component.translatable("caa.schedule.threshold.buckets"));

        caa$inStacksOrBuckets = (SelectionScrollInput) new SelectionScrollInput(x + 100, y + 21, 52, 36)
            .forOptions(selections)
            .titled(Component.translatable("caa.schedule.threshold.measure"))
            .calling(state -> lastModification = 0)
            .setState(ex.caa$inStacksOrBuckets() ? 1 : 0);

        caa$offBelow = new ScrollInput(x + 48, y + 47, 48, 18)
            .withRange(ex.caa$getMinAmount(), ex.caa$getMaxAmount() + 1 - caa$getValueStep())
            .titled(Component.translatable("caa.gui.threshold.lower_threshold"))
            .calling(state -> {
                lastModification = 0;
                int valueStep = caa$getValueStep();

                if (caa$onAbove.getState() / valueStep == 0 && state / valueStep == 0)
                    return;

                if (caa$onAbove.getState() / valueStep <= state / valueStep) {
                    caa$onAbove.setState((state + valueStep) / valueStep * valueStep);
                    caa$onAbove.onChanged();
                }
            })
            .withStepFunction(sc -> (sc.shift ? 10 : 1) * caa$getValueStep())
            .setState(ex.caa$getOffWhenBelow());

        caa$onAbove = new ScrollInput(x + 48, y + 23, 48, 18)
            .withRange(ex.caa$getMinAmount() + caa$getValueStep(), ex.caa$getMaxAmount() + 1)
            .titled(Component.translatable("caa.gui.threshold.upper_threshold"))
            .calling(state -> {
                lastModification = 0;
                int valueStep = caa$getValueStep();

                if (caa$offBelow.getState() / valueStep == 0 && state / valueStep == 0)
                    return;

                if (caa$offBelow.getState() / valueStep >= state / valueStep) {
                    caa$offBelow.setState((state - valueStep) / valueStep * valueStep);
                    caa$offBelow.onChanged();
                }
            })
            .withStepFunction(sc -> (sc.shift ? 10 : 1) * caa$getValueStep())
            .setState(ex.caa$getOnWhenAbove());

        caa$onAbove.onChanged();
        caa$offBelow.onChanged();

        caa$setMode(ex.caa$isPrecision());

        addRenderableWidgets(changeModeButton, caa$inStacksOrBuckets, caa$onAbove, caa$offBelow);
    }

    @Inject(method = "tick", at = @At("TAIL"), remap = true)
    private void afterTick(CallbackInfo ci) {
        if (!caa$precision)
            return;

        boolean highlightTopRow = blockEntity.isInverted() ^ blockEntity.isPowered();
        caa$cursor.chase(highlightTopRow ? 0 : 1, 1/ 4f, LerpedFloat.Chaser.EXP);
        caa$cursor.tickChaser();

        ThresholdSwitchBlockEntityEx ex = (ThresholdSwitchBlockEntityEx) blockEntity;
        ThresholdType type = ex.caa$getTypeOfCurrentTarget();
        boolean forItemsOrFluid = type == ThresholdType.ITEM || type == ThresholdType.FLUID;
        caa$inStacksOrBuckets.active = caa$inStacksOrBuckets.visible = forItemsOrFluid;

        if (type == ThresholdType.UNSUPPORTED) {
            caa$onAbove.active = caa$onAbove.visible = false;
            caa$offBelow.active = caa$offBelow.visible = false;
            return;
        } else {
            caa$onAbove.active = caa$onAbove.visible = true;
            caa$offBelow.active = caa$offBelow.visible = true;
        }

        caa$onAbove.setWidth(forItemsOrFluid ? 48 : 103);
        caa$offBelow.setWidth(forItemsOrFluid ? 48 : 103);

        int valueStep = caa$getValueStep();
        int min = ex.caa$getMinAmount() + valueStep;
        int max = ex.caa$getMaxAmount();
        caa$onAbove.withRange(min, max + 1);
        int roundedState = Mth.clamp((caa$onAbove.getState() / valueStep) * valueStep, min, max);
        if (roundedState != caa$onAbove.getState()) {
            caa$onAbove.setState(roundedState);
            caa$onAbove.onChanged();
        }

        min = ex.caa$getMinAmount();
        max = ex.caa$getMaxAmount() - valueStep;
        caa$offBelow.withRange(min, max + 1);
        roundedState = Mth.clamp((caa$offBelow.getState() / valueStep) * valueStep, min, max);
        if (roundedState != caa$offBelow.getState()) {
            caa$offBelow.setState(roundedState);
            caa$offBelow.onChanged();
        }
    }

    @Inject(method = "renderWindow", at = @At("HEAD"), cancellable = true)
    private void renderWindowEx(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!caa$precision)
            return;
        ci.cancel();

        int x = guiLeft;
        int y = guiTop;
        ThresholdSwitchBlockEntityEx ex = (ThresholdSwitchBlockEntityEx) blockEntity;
        ThresholdType type = ex.caa$getTypeOfCurrentTarget();
        boolean forItemsOrFluid = type == ThresholdType.ITEM || type == ThresholdType.FLUID;

        ThresholdSwitchScreenBackground.render(graphics, x, y, forItemsOrFluid);
        graphics.drawString(font, title, x + (background.width - 8) / 2 - font.width(title) / 2, y + 4, 0x592424, false);

        boolean stacksOrBuckets = caa$inStacksOrBuckets.getState() == 1;
        if (type == ThresholdType.ITEM) {
            Component suffix = stacksOrBuckets ?
                Component.translatable("caa.schedule.threshold.stacks") :
                Component.translatable("caa.schedule.threshold.items");
            graphics.drawString(font, suffix, x + 103, y + 26, 0xFFFFFFFF, true);
            graphics.drawString(font, suffix, x + 103, y + 46, 0xFFFFFFFF, true);
        } else if (type == ThresholdType.FLUID) {
            Component suffix = stacksOrBuckets ?
                Component.translatable("caa.schedule.threshold.buckets") :
                Component.translatable("caa.schedule.threshold.milibuckets");
            graphics.drawString(font, suffix, x + 103, y + 26, 0xFFFFFFFF, true);
            graphics.drawString(font, suffix, x + 103, y + 46, 0xFFFFFFFF, true);
        }

        int valueStep = caa$getValueStep();

        graphics.drawString(font,
            Component.literal("≥ " + (type == ThresholdType.UNSUPPORTED ? "" :
                forItemsOrFluid ? caa$onAbove.getState() / valueStep :
                    ex.caa$format(caa$onAbove.getState() / valueStep, stacksOrBuckets).getString())),
            x + 51, y + 26, 0xFFFFFFFF, true);
        graphics.drawString(font,
            Component.literal("≤ " + (type == ThresholdType.UNSUPPORTED ? "" :
                forItemsOrFluid ? caa$offBelow.getState() / valueStep :
                    ex.caa$format(caa$offBelow.getState() / valueStep, stacksOrBuckets).getString())),
            x + 51, y + 46, 0xFFFFFFFF, true);

        GuiGameElement.of(renderedItem)
            .<GuiGameElement.GuiRenderBuilder>at(x + background.width + 6, y + background.height - 56, -200)
            .scale(5)
            .render(graphics);

        int itemX = x + 13;
        int itemY = y + 72;

        ItemStack displayItem = ex.caa$getDisplayItemForScreen();
        GuiGameElement.of(displayItem.isEmpty() ? new ItemStack(Items.BARRIER) : displayItem)
            .<GuiGameElement.GuiRenderBuilder>at(itemX, itemY, 0)
            .render(graphics);

        int torchX = x + 21;
        int torchY = y + 22;

        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(0, caa$cursor.getValue(partialTicks) * 20, 0);
        AllGuiTextures.STOCKSWITCH_CURSOR.render(graphics, x + 18, y + 22);
        ms.popPose();

        ms.pushPose();
        ms.translate(torchX - 5, torchY + 14, 200);
        ms.mulPose(Axis.XP.rotationDegrees(-22.5f));
        ms.mulPose(Axis.YP.rotationDegrees(45));
        for (boolean power : Iterate.trueAndFalse) {
            GuiGameElement.of(Blocks.REDSTONE_TORCH.defaultBlockState()
                    .setValue(RedstoneTorchBlock.LIT, blockEntity.isInverted() ^ power))
                .scale(20)
                .render(graphics);
            ms.translate(0, 22, 0);
        }
        ms.popPose();

        if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
            ArrayList<Component> list = new ArrayList<>();
            if (displayItem.isEmpty()) {
                list.add(Component.translatable("caa.gui.threshold.not_attached"));
                graphics.renderComponentTooltip(font, list, mouseX, mouseY);
                return;
            }

            list.add(displayItem.getHoverName());
            if (type == ThresholdType.UNSUPPORTED) {
                list.add(Component.translatable("caa.gui.threshold.incompatible")
                    .withStyle(ChatFormatting.GRAY));
                graphics.renderComponentTooltip(font, list, mouseX, mouseY);
                return;
            }

            list.add(Component.translatable("caa.gui.threshold.currently",
                    ex.caa$format(ex.caa$getCurrentAmount() / valueStep, stacksOrBuckets))
                .withStyle(ChatFormatting.DARK_AQUA));

            if (ex.caa$getMinAmount() / valueStep == 0) {
                list.add(Component.translatable("caa.gui.threshold.range_max",
                        ex.caa$format(ex.caa$getMaxAmount() / valueStep, stacksOrBuckets))
                    .withStyle(ChatFormatting.GRAY));
            } else {
                list.add(Component.translatable("caa.gui.threshold.range",
                        ex.caa$getMinAmount() / valueStep,
                        ex.caa$format(ex.caa$getMaxAmount() / valueStep, stacksOrBuckets))
                    .withStyle(ChatFormatting.GRAY));
            }

            graphics.renderComponentTooltip(font, list, mouseX, mouseY);
            return;
        }

        for (boolean power : Iterate.trueAndFalse) {
            int thisTorchY = power ? torchY : torchY + 22;
            if (mouseX >= torchX && mouseX < torchX + 16 && mouseY >= thisTorchY && mouseY < thisTorchY + 16) {
                graphics.renderComponentTooltip(font,
                    List.of(Component.translatable(power ^ blockEntity.isInverted() ?
                            "caa.gui.threshold.power_on_when" :
                            "caa.gui.threshold.power_off_when")
                        .withStyle(ct -> ct.withColor(AbstractSimiWidget.HEADER_RGB))),
                    mouseX, mouseY);
                break;
            }
        }
    }

    @Unique
    private void caa$setMode(boolean mode) {
        lastModification = 0;
        caa$precision = mode;
        if (!mode) {
            caa$inStacksOrBuckets.active = caa$inStacksOrBuckets.visible = false;
            caa$onAbove.active = caa$onAbove.visible = false;
            caa$offBelow.active = caa$offBelow.visible = false;
        }
        onAbove.active = onAbove.visible = !mode;
        offBelow.active = offBelow.visible = !mode;
    }

    @Unique
    private int caa$getValueStep() {
        boolean stacksOrBuckets = caa$inStacksOrBuckets.getState() == 1;
        ThresholdType type = ((ThresholdSwitchBlockEntityEx) blockEntity).caa$getTypeOfCurrentTarget();
        int valueStep;
        if (type == ThresholdType.ITEM) {
            valueStep = stacksOrBuckets ? 64 : 1;
        } else if (type == ThresholdType.FLUID) {
            valueStep = stacksOrBuckets ? 1000 : 1;
        } else {
            valueStep = 1;
        }
        return valueStep;
    }
    
    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void sendPrecisionPacket(boolean invert, CallbackInfo ci) {
        if (caa$precision) {
            AllPackets.getChannel().sendToServer(new ConfigurePreciseThresholdSwitchPacket(
                blockEntity.getBlockPos(), caa$offBelow.getState(), caa$onAbove.getState(),
                invert, caa$inStacksOrBuckets.getState() == 1));
            ci.cancel();
        }
    }
}
