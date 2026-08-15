package net.apertyotis.createandesiteabound.content.liquid.vessel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class FluidVesselItem extends BlockItem {

    public FluidVesselItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    public static FluidStack getFluid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            return FluidStack.loadFluidStackFromNBT(tag.getCompound("Content"));
        }
        return FluidStack.EMPTY;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!stack.hasTag()) {
            entity.discard();
            return true;
        }
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        FluidStack fluid = getFluid(stack);
        if (!fluid.isEmpty()) {
            Component component = fluid.getDisplayName().copy()
                .withStyle(ChatFormatting.GRAY)
                .append(" ")
                .append(Component.literal(String.valueOf(fluid.getAmount()))
                    .withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" / %d mB".formatted(AllConfig.fluid_vessel_capacity * 1000))
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(component);
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new VesselFluidHandlerItemStack(stack, AllConfig.fluid_vessel_capacity * 1000);
    }

    public static class VesselFluidHandlerItemStack extends FluidHandlerItemStack {

        public VesselFluidHandlerItemStack(@NotNull ItemStack container, int capacity) {
            super(container, capacity);
        }

        @Override
        public @NotNull FluidStack getFluid() {
            return FluidVesselItem.getFluid(container);
        }

        @Override
        protected void setFluid(FluidStack fluid)
        {
            if (fluid.isEmpty()) {
                setContainerToEmpty();
            } else {
                CompoundTag tag = container.getOrCreateTag();
                tag.put("Content", fluid.writeToNBT(new CompoundTag()));
            }
        }

        @Override
        protected void setContainerToEmpty()
        {
            CompoundTag tag = container.getTag();
            if (tag != null) {
                tag.remove("Content");
                if (tag.getAllKeys().isEmpty()) {
                    container.setTag(null);
                }
            }
        }
    }

    public static class VesselItemDecorator implements IItemDecorator {
        @Override
        public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
            FluidStack fluid = FluidVesselItem.getFluid(stack);
            if (fluid.isEmpty())
                return false;

            int x = xOffset + 2;
            int y = yOffset + 1;
            int height = Math.round(14f * fluid.getAmount() / 1000 / AllConfig.fluid_vessel_capacity);
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(0, 0, 199.9f);
            guiGraphics.fill(x, y, x + 2, y + 14, 0xFF000000);
            guiGraphics.fill(x, y + 14 - height, x + 1, y + 14, 0xFF00FF00);
            pose.popPose();
            return true;
        }
    }
}
