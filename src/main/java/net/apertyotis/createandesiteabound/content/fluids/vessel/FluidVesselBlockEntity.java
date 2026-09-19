package net.apertyotis.createandesiteabound.content.fluids.vessel;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FluidVesselBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public VesselTankHandler tank;
    int soundCooldown;
    int syncCooldown;
    boolean queuedSync;
    LazyOptional<IItemHandler> itemHandler;
    LazyOptional<IFluidHandler> tankHandler;

    public FluidVesselBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        itemHandler = LazyOptional.of(VesselItemHandler::new);
        tank = new VesselTankHandler();
        tankHandler = LazyOptional.of(() -> tank);
    }

    public void playVoidingSound() {
        if (soundCooldown == 0 && level instanceof ServerLevel) {
            soundCooldown = 10;
            level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS, .3f, 1.5f + .5f * level.getRandom().nextFloat());
        }
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.put("Content", tank.fluid.writeToNBT(new CompoundTag()));
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        tank.fluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("Content"));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        itemHandler.invalidate();
        tankHandler.invalidate();
    }

    @Override
    public void tick() {
        super.tick();
        if (soundCooldown > 0)
            soundCooldown--;
        if (syncCooldown > 0) {
            syncCooldown--;
            if (syncCooldown == 0 && queuedSync) {
                queuedSync = false;
                notifyUpdate();
            }
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this).allowingBeltFunnels());
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking, getCapability(ForgeCapabilities.FLUID_HANDLER));
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return tankHandler.cast();
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
            if (tank.canVoid()) {
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

    public class VesselTankHandler implements IFluidHandler {
        private FluidStack fluid = FluidStack.EMPTY;
        private final int capacity;

        public VesselTankHandler() {
            capacity = AllConfig.fluid_vessel_capacity * 1000;
        }

        public boolean canVoid() {
            return fluid.getFluid().getFluidType() == ForgeMod.LAVA_TYPE.get();
        }

        public void setFluid(FluidStack fluid) {
            this.fluid = fluid;
            onContentChange();
        }

        public void onContentChange() {
            FluidVesselBlockEntity be = FluidVesselBlockEntity.this;
            if (be.level == null || be.level.isClientSide)
                return;
            if (be.syncCooldown > 0) {
                be.queuedSync = true;
            } else {
                be.syncCooldown = 8;
                be.queuedSync = false;
                be.notifyUpdate();
            }
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return fluid;
        }

        @Override
        public int getTankCapacity(int tank) {
            return capacity;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return fluid.isEmpty() || fluid.isFluidEqual(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty())
                return 0;
            if (fluid.isEmpty()) {
                int toFill = Math.min(resource.getAmount(), capacity);
                if (action.execute()) {
                    fluid = resource.copy();
                    fluid.setAmount(toFill);
                    onContentChange();
                }
                return toFill;
            } else if (canVoid()) {
                if (action.execute()) {
                    if (fluid.isFluidEqual(resource)) {
                        int toFill = Math.min(resource.getAmount(), capacity - fluid.getAmount());
                        fluid.grow(toFill);
                        if (toFill < resource.getAmount())
                            FluidVesselBlockEntity.this.playVoidingSound();
                    } else {
                        FluidVesselBlockEntity.this.playVoidingSound();
                    }
                    onContentChange();
                }
                return resource.getAmount();
            } else if (fluid.isFluidEqual(resource)) {
                int toFill = Math.min(resource.getAmount(), capacity - fluid.getAmount());
                if (action.execute()) {
                    fluid.grow(toFill);
                    onContentChange();
                }
                return toFill;
            }
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (!fluid.isFluidEqual(resource) || fluid.isEmpty())
                return FluidStack.EMPTY;
            FluidStack toDrain = fluid.copy();
            toDrain.setAmount(Math.min(fluid.getAmount(), resource.getAmount()));
            if (action.execute()) {
                fluid.shrink(toDrain.getAmount());
                onContentChange();
            }
            return toDrain;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0 || fluid.isEmpty())
                return FluidStack.EMPTY;
            FluidStack toDrain = fluid.copy();
            toDrain.setAmount(Math.min(fluid.getAmount(), maxDrain));
            if (action.execute()) {
                fluid.shrink(toDrain.getAmount());
                onContentChange();
            }
            return toDrain;
        }
    }
}
