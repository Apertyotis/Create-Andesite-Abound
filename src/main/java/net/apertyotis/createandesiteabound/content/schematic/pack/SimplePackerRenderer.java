package net.apertyotis.createandesiteabound.content.schematic.pack;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import static net.apertyotis.createandesiteabound.CreateAndesiteAbound.MOD_ID;

@SuppressWarnings("removal")
public class SimplePackerRenderer extends CustomRenderedItemModelRenderer {
    private static final PartialModel SLIME =
        new PartialModel(new ResourceLocation(MOD_ID, "item/simple_packer/slime"));
    private static final PartialModel LID =
        new PartialModel(new ResourceLocation(MOD_ID, "item/simple_packer/lid"));
    private static final PartialModel SLIMELI_ =
        new PartialModel(new ResourceLocation(MOD_ID, "item/simple_packer/slimeli_"));
    private static final PartialModel ANDESITE_ABOUND =
        new PartialModel(new ResourceLocation(MOD_ID, "item/simple_packer/andesiteabound"));
    private static final PartialModel FLY_MACHINE =
        new PartialModel(new ResourceLocation(MOD_ID, "item/simple_packer/fly_machine"));
    private static final PartialModel XKMXZ2503 =
        new PartialModel(new ResourceLocation(MOD_ID, "item/simple_packer/xkmxz2503"));

    @Override
    protected void render(
        ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
        ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay
    ) {
        CompoundTag tag = stack.hasTag() ? stack.getTag() : null;
        if (tag != null && tag.getBoolean("Closure")) {
            ms.translate(0, -1f / 16, 0);
            renderer.render(model.getOriginalModel(), light);
            renderer.render(LID.get(), light);
        } else {
            renderer.render(model.getOriginalModel(), light);
            float partial = AnimationTickHolder.getPartialTicks();
            float degree = SimplePackerHandler.SIMPLE_PACKER_HANDLER.getScroll(partial);
            float yOffset = SimplePackerHandler.SIMPLE_PACKER_HANDLER.height.getValue(partial);
            ms.mulPose(Axis.YP.rotationDegrees(degree));
            ms.translate(0, yOffset, 0);
            int variant = tag != null ? tag.getInt("Variant") : 0;
            switch (variant) {
                case 1 -> renderer.render(SLIMELI_.get(), light);
                case 2 -> renderer.render(ANDESITE_ABOUND.get(), light);
                case 3 -> renderer.render(FLY_MACHINE.get(), light);
                case 4 -> renderer.render(XKMXZ2503.get(), light);
                default -> renderer.render(SLIME.get(), light);
            }
        }
    }
}
