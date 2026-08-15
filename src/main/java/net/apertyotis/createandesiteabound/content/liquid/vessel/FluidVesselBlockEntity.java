package net.apertyotis.createandesiteabound.content.liquid.vessel;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FluidVesselBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    SmartFluidTankBehaviour tank;
    int soundCooldown;
    LazyOptional<IItemHandler> itemHandler;

    public FluidVesselBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        itemHandler = LazyOptional.of(VesselItemHandler::new);
    }

    public boolean canVoidItem() {
        return tank.getPrimaryHandler().getFluid().getFluid().getFluidType() == ForgeMod.LAVA_TYPE.get();
    }

    public void playVoidingSound() {
        if (soundCooldown == 0 && level instanceof ServerLevel) {
            soundCooldown = 60;
            level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        itemHandler.invalidate();
    }

    @Override
    public void tick() {
        if (soundCooldown > 0)
            soundCooldown--;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, AllConfig.fluid_vessel_capacity * 1000);
        behaviours.add(tank);
        behaviours.add(new DirectBeltInputBehaviour(this).allowingBeltFunnels());
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking, getCapability(ForgeCapabilities.FLUID_HANDLER));
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return tank.getCapability().cast();
        else if (cap == ForgeCapabilities.ITEM_HANDLER)
            return itemHandler.cast();
        return super.getCapability(cap, side);
    }

    class VesselItemHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (canVoidItem()) {
                if (!simulate)
                    playVoidingSound();
                return ItemStack.EMPTY;
            }
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true;
        }
    }
}
