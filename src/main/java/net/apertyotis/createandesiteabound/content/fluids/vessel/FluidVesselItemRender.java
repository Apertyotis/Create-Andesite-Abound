package net.apertyotis.createandesiteabound.content.fluids.vessel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class FluidVesselItemRender extends CustomRenderedItemModelRenderer {
    @Override
    protected void render(
        ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
        ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay
    ) {
        renderer.render(model.getOriginalModel(), light);

        FluidStack fluidStack = FluidVesselItem.getFluid(stack);
        if (fluidStack.isEmpty())
            return;

        ms.pushPose();
        ms.translate(-0.5f, -0.5f, -0.5f);
        ms.translate(5.7 / 16, 14f / 16, 5.7 / 16);
        FluidRenderer.renderFluidBox(fluidStack, 0f, 0f, 0f, 4.6f / 16, 0f, 4.6f / 16,
            buffer, ms, light, false);
        ms.popPose();
    }
}
