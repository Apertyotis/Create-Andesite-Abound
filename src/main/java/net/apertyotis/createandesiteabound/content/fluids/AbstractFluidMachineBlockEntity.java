package net.apertyotis.createandesiteabound.content.fluids;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.*;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Lang;
import net.apertyotis.createandesiteabound.AllBlocks;
import net.apertyotis.createandesiteabound.content.fluids.vessel.FluidVesselItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractFluidMachineBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    protected VersionedInventoryTrackerBehaviour invVersionTracker;
    protected InvManipulationBehaviour targetInv;
    protected TankManipulationBehaviour targetTank;
    protected FluidMachineItemHandler inventory;
    protected VersionedInventoryWrapper invWrapper;
    protected LazyOptional<VersionedInventoryWrapper> capProvider;
    protected int cooldown = 0;

    public AbstractFluidMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory = new FluidMachineItemHandler(this);
        invWrapper = new VersionedInventoryWrapper(inventory);
        capProvider = LazyOptional.of(() -> invWrapper);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER)
            return capProvider.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        invVersionTracker = new VersionedInventoryTrackerBehaviour(this);
        targetInv = new InvManipulationBehaviour(this,
            CapManipulationBehaviourBase.InterfaceProvider.towardBlockFacing())
            .bypassSidedness();
        targetTank = new TankManipulationBehaviour(this, (w, p, b) -> new BlockFace(p, getTarget()))
            .bypassSidedness();

        behaviours.add(invVersionTracker);
        behaviours.add(targetInv);
        behaviours.add(targetTank);
    }

    public abstract Direction getTarget();

    @Override
    public void invalidate() {
        super.invalidate();
        capProvider.invalidate();
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        ListTag list = new ListTag();
        for (int i = 0; i < inventory.items.size(); i++) {
            ItemStack stack = inventory.items.get(i);
            list.add(stack.serializeNBT());
        }
        if (!list.isEmpty())
            tag.put("Items", list);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        inventory.items.clear();
        if (tag.contains("Items", ListTag.TAG_LIST)) {
            ListTag list = tag.getList("Items", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < list.size() && i < inventory.items.capacity(); i++)
                inventory.items.addLast(ItemStack.of(list.getCompound(i)));
        }
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (inventory.items.isEmpty())
            return false;

        Lang.builder("")
            .add(Component.translatable("info.caa.goggle.fluid_machine_content"))
            .forGoggles(tooltip);

        int count = 0;
        ItemStack last = null;
        for (int i = 0; i < inventory.items.size(); i++) {
            ItemStack stack = inventory.items.get(i);
            if (AllBlocks.FLUID_VESSEL.isIn(stack)) {
                if (last == null) {
                    last = stack;
                    count++;
                } else if (last.equals(stack, true)) {
                    count++;
                } else {
                    vesselContentTooltip(tooltip, last, count);
                    last = stack;
                    count = 1;
                }
            }
        }
        if (last != null && count != 0) {
            vesselContentTooltip(tooltip, last, count);
        }

        return true;
    }

    private static void vesselContentTooltip(List<Component> tooltip, ItemStack stack, int count) {
        FluidStack fluid = FluidVesselItem.getFluid(stack);
        Lang.builder("")
            .add(Component.literal("["))
            .add(fluid.getDisplayName().copy()
                .withStyle(ChatFormatting.GRAY))
            .add(Component.literal(" %dmB".formatted(fluid.getAmount()))
                .withStyle(ChatFormatting.GOLD))
            .add(Component.literal("] x%d".formatted(count)))
            .forGoggles(tooltip, 1);
    }
}
