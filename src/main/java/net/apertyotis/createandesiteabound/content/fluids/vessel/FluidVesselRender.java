package net.apertyotis.createandesiteabound.content.fluids.vessel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.fluids.FluidStack;

public class FluidVesselRender extends SafeBlockEntityRenderer<FluidVesselBlockEntity> {

    public FluidVesselRender(BlockEntityRendererProvider.Context ignored) {}

    @Override
    protected void renderSafe(
        FluidVesselBlockEntity be, float partialTicks, PoseStack ms,
        MultiBufferSource buffer, int light, int overlay
    ) {
        FluidStack fluidStack = be.tank.getPrimaryHandler().getFluid();
        if (fluidStack.isEmpty())
            return;

        ms.pushPose();
        ms.translate(5.7f / 16, 14f / 16, 5.7f / 16);
        FluidRenderer.renderFluidBox(fluidStack, 0f, 0f, 0f, 4.6f / 16, 0f, 4.6f / 16,
            buffer, ms, light, false);
        ms.popPose();
    }
}
