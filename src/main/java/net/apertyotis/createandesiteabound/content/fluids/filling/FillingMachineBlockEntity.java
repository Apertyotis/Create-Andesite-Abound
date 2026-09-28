package net.apertyotis.createandesiteabound.content.fluids.filling;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import net.apertyotis.createandesiteabound.AllConfig;
import net.apertyotis.createandesiteabound.content.fluids.AbstractFluidMachineBlock;
import net.apertyotis.createandesiteabound.content.fluids.AbstractFluidMachineBlockEntity;
import net.apertyotis.createandesiteabound.content.fluids.vessel.FluidVesselItem;
import net.apertyotis.createandesiteabound.foundation.CircularArray;
import net.apertyotis.createandesiteabound.mixin.create.foundation.blockEntity.FilteringBehaviourAccessor;
import net.apertyotis.createandesiteabound.mixin.create.logistics.filter.FilterItemStackAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;


public class FillingMachineBlockEntity extends AbstractFluidMachineBlockEntity {

    private FillingAmountBehaviour filter;

    public FillingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory.onlyOutput();
    }

    @Override
    public Direction getTarget() {
        return Direction.UP;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        filter = new FillingAmountBehaviour(
            this, new CenteredSideValueBoxTransform((state, side) ->
            side.getAxis().isHorizontal() && !side.equals(state.getValue(AbstractFluidMachineBlock.FACING))));
        behaviours.add(filter);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("Cooldown", cooldown);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        cooldown = tag.getInt("Cooldown");
    }

    @Override
    public void tick() {
        super.tick();
        cooldown--;
        if (cooldown > 0)
            return;
        cooldown = 5;

        CircularArray<ItemStack> items = inventory.items;
        Filling:
        if (getBlockState().getValue(FillingMachineBlock.POWERED)) {
            if (items.size() >= items.capacity() || !targetTank.hasInventory())
                break Filling;
            IFluidHandler handler = targetTank.getInventory();
            if (handler == null)
                break Filling;

            boolean precise = filter.count != 0;
            int amount = precise ? filter.count : AllConfig.fluid_vessel_capacity * 1000;
            FluidStack resolvedFilter = resolveFilter(level, filter);
            FluidStack toExtract = FluidStack.EMPTY;
            if (resolvedFilter != null) {
                if (resolvedFilter.isEmpty())
                    break Filling;
                resolvedFilter.setAmount(amount);
                toExtract = handler.drain(resolvedFilter, FluidAction.SIMULATE);
                if (toExtract.isEmpty() || (precise && toExtract.getAmount() < amount))
                    toExtract = FluidStack.EMPTY;
            } else {
                for (int i = 0; i < handler.getTanks(); i++) {
                    FluidStack fluid = handler.getFluidInTank(i);
                    if (fluid.isEmpty() || !filter.test(fluid))
                        continue;

                    fluid = fluid.copy();
                    fluid.setAmount(precise ? amount : Math.min(amount, fluid.getAmount()));
                    toExtract = handler.drain(fluid, FluidAction.SIMULATE);
                    if (toExtract.isEmpty() || (precise && toExtract.getAmount() < amount))
                        toExtract = FluidStack.EMPTY;
                    else
                        break;
                }
            }

            if (!toExtract.isEmpty()) {
                handler.drain(toExtract, FluidAction.EXECUTE);
                items.addLast(FluidVesselItem.of(toExtract));
                notifyUpdate();
                invWrapper.incrementVersion();
                if (level instanceof ServerLevel)
                    level.playSound(null, worldPosition, SoundEvents.BUCKET_FILL,
                        SoundSource.BLOCKS, .3f, 1.5f + .5f * level.getRandom().nextFloat());
            }
        }

        if (!targetInv.hasInventory() || invVersionTracker.stillWaiting(targetInv))
            return;
        IItemHandler handler = targetInv.getInventory();
        boolean changed = false;
        while (!items.isEmpty()) {
            ItemStack result = ItemHandlerHelper.insertItem(handler, items.getFirst(), false);
            if (result.isEmpty()) {
                items.removeFirst();
                changed = true;
            } else {
                break;
            }
        }
        if (changed) {
            notifyUpdate();
            invWrapper.incrementVersion();
            invVersionTracker.awaitNewVersion(handler);
        }
    }

    public static FluidStack resolveFilter(Level level, FillingAmountBehaviour filter) {
        FilterItemStack filterItem = ((FilteringBehaviourAccessor) filter).getFilterItemStack();
        if (filterItem.isEmpty() || filterItem.getClass() != FilterItemStack.class)
            return null;
        FilterItemStackAccessor accessor = (FilterItemStackAccessor) filterItem;
        accessor.invokeResolveFluid(level);
        return accessor.getFilterFluidStack().copy();
    }
}
