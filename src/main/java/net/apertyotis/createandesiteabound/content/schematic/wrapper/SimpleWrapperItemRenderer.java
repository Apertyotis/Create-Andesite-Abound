package net.apertyotis.createandesiteabound.content.schematic.wrapper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SimpleWrapperItemRenderer extends CustomRenderedItemModelRenderer {

    @Override
    protected void render(
        ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
        ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay
    ) {
        LocalPlayer player = Minecraft.getInstance().player;
        float partialTicks = AnimationTickHolder.getPartialTicks();

        CompoundTag tag = stack.getTag();
        boolean jeiMode = tag != null && tag.contains("JEI");

        if (tag != null && tag.contains("Polishing") &&
            (transformType.firstPerson() || transformType == ItemDisplayContext.GUI)
        ) {
            ms.pushPose();
            if (transformType == ItemDisplayContext.GUI) {
                ms.mulPose(Axis.YP.rotationDegrees(135));
                ms.mulPose(Axis.XP.rotationDegrees(-30));
                ms.translate(0, .2f, 1.0F);
                ms.scale(.75f, .75f, .75f);
                ms.mulPose(Axis.XP.rotationDegrees(30));
                ms.mulPose(Axis.YP.rotationDegrees(45));
            } else {
                ms.mulPose(Axis.YP.rotationDegrees(120));
                ms.translate(-0.5f, 0.4f, -0.4f);
            }

            // Reverse bobbing
            float time = (float) (!jeiMode ? (player != null ? player.getUseItemRemainingTicks() : 0)
                : (-AnimationTickHolder.getTicks()) % stack.getUseDuration()) - partialTicks + 1.0F;
            if (time / (float) stack.getUseDuration() < 0.8F) {
                float bobbing = -Mth.abs(Mth.cos(time / 4.0F * (float) Math.PI) * 0.25F);

                if (transformType == ItemDisplayContext.GUI)
                    ms.translate(bobbing, bobbing, 0.0F);
                else
                    ms.translate(0.0f, bobbing, 0.0F);
            }
            renderer.render(model.getOriginalModel(), light);
            ms.popPose();

            ms.pushPose();
            if (player != null && transformType != ItemDisplayContext.GUI) {
                int itemInUseCount = player.getUseItemRemainingTicks();
                if (itemInUseCount > 0) {
                    ms.scale(2.5f, 2.5f, 2.5f);
                    ms.mulPose(Axis.YP.rotationDegrees(-45));
                    ms.translate(0.6f, 0f, -.1f);
                    ms.mulPose(Axis.YP.rotationDegrees(-50));
                    ms.mulPose(Axis.ZP.rotationDegrees(20));
                    ms.mulPose(Axis.XP.rotationDegrees(-20));
                }
            } else {
                ms.mulPose(Axis.YP.rotationDegrees(135));
                ms.mulPose(Axis.XP.rotationDegrees(-30));
                ms.scale(1.6f, 1.6f, 1.6f);
            }

            ItemStack blueprint = AllItems.SCHEMATIC.asStack();
            renderer.renderGlowing(Minecraft.getInstance().getItemRenderer()
                .getModel(blueprint, null, null, 0), LightTexture.FULL_BRIGHT);
            ms.popPose();
        } else {
            renderer.render(model.getOriginalModel(), light);
        }
    }

}
