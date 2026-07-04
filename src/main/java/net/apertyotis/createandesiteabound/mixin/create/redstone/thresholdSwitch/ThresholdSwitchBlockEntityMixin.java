package net.apertyotis.createandesiteabound.mixin.create.redstone.thresholdSwitch;

import com.simibubi.create.compat.thresholdSwitch.ThresholdSwitchCompat;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlock;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import net.apertyotis.createandesiteabound.content.thresholdSwitch.ThresholdSwitchBlockEntityEx;
import net.apertyotis.createandesiteabound.content.thresholdSwitch.ThresholdSwitchObservableEx;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ThresholdSwitchBlockEntity.class, remap = false)
public abstract class ThresholdSwitchBlockEntityMixin implements ThresholdSwitchBlockEntityEx {
    @Shadow
    private InvManipulationBehaviour observedInventory;
    @Shadow
    private TankManipulationBehaviour observedTank;
    @Shadow
    private VersionedInventoryTrackerBehaviour invVersionTracker;
    @Shadow
    private FilteringBehaviour filtering;
    @Shadow
    private boolean redstoneState;

    @Shadow
    protected abstract void scheduleBlockTick();

    @Shadow
    @Final
    private static List<ThresholdSwitchCompat> COMPAT;
    @Unique
    private boolean caa$inStacksOrBuckets = false;
    @Unique
    private boolean caa$precision = false;
    @Unique
    private int caa$minAmount = 0;
    @Unique
    private int caa$maxAmount = 0;
    @Unique
    private int caa$onWhenAbove = 128;
    @Unique
    private int caa$offWhenBelow = 64;
    @Unique
    private int caa$currentAmount = -1;

    @Unique
    @Override
    public boolean caa$inStacksOrBuckets() {
        return caa$inStacksOrBuckets;
    }

    @Unique
    @Override
    public boolean caa$isPrecision() {
        return caa$precision;
    }

    @Unique
    @Override
    public int caa$getMinAmount() {
        return caa$minAmount;
    }

    @Unique
    @Override
    public int caa$getCurrentAmount() {
        return caa$currentAmount;
    }

    @Unique
    @Override
    public int caa$getMaxAmount() {
        return caa$maxAmount;
    }

    @Unique
    @Override
    public int caa$getOnWhenAbove() {
        return caa$onWhenAbove;
    }

    @Unique
    @Override
    public int caa$getOffWhenBelow() {
        return caa$offWhenBelow;
    }

    @Unique
    @Override
    public void caa$setPrecision(boolean precision) {
        caa$precision = precision;
    }

    @Unique
    @Override
    public void caa$configure(int offBelow, int onAbove, boolean invert, boolean inStacksOrBuckets) {
        caa$precision = true;
        caa$offWhenBelow = offBelow;
        caa$onWhenAbove = onAbove;
        ((ThresholdSwitchBlockEntity)(Object) this).setInverted(invert);
        caa$inStacksOrBuckets = inStacksOrBuckets;
        invVersionTracker.awaitNewVersion(observedInventory);
    }

    @Unique
    @Override
    public ThresholdType caa$getTypeOfCurrentTarget() {
        if (observedInventory.hasInventory())
            return ThresholdType.ITEM;
        if (observedTank.hasInventory())
            return ThresholdType.FLUID;

        Level level = ((BlockEntity)(Object) this).getLevel();
        if (level != null && level.getBlockEntity(caa$getTargetPos()) instanceof ThresholdSwitchObservableEx)
            return ThresholdType.CUSTOM;

        return ThresholdType.UNSUPPORTED;
    }

    @Unique
    @Override
    public ItemStack caa$getDisplayItemForScreen() {
        Level level = ((BlockEntity)(Object) this).getLevel();
        if (level == null)
            return ItemStack.EMPTY;
        return new ItemStack(level.getBlockState(caa$getTargetPos()).getBlock());
    }

    @Unique
    @Override
    public MutableComponent caa$format(int value, boolean stacksOrBuckets) {
        ThresholdType type = caa$getTypeOfCurrentTarget();
        Level level = ((BlockEntity)(Object) this).getLevel();
        if (type == ThresholdType.CUSTOM && level != null &&
            level.getBlockEntity(caa$getTargetPos()) instanceof ThresholdSwitchObservableEx tsoEx)
            return tsoEx.caa$format(value);

        String suffix;
        if (type == ThresholdType.ITEM)
            suffix = stacksOrBuckets ? "caa.schedule.threshold.stacks" : "caa.schedule.threshold.items";
        else
            suffix = stacksOrBuckets ? "caa.schedule.threshold.buckets" : "caa.schedule.threshold.milibuckets";

        return Component.literal(value + " ").append(Component.translatable(suffix));
    }

    @Unique
    private BlockPos caa$getTargetPos() {
        BlockEntity be = (BlockEntity)(Object) this;
        return be.getBlockPos().relative(ThresholdSwitchBlock.getTargetDirection(be.getBlockState()));
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void readEx(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        caa$precision = compound.getBoolean("Precision");
        caa$onWhenAbove = compound.getInt("OnAboveAmount");
        caa$offWhenBelow = compound.getInt("OffBelowAmount");
        caa$currentAmount = compound.getInt("CurrentAmount");
        caa$minAmount = compound.getInt("CurrentMinAmount");
        caa$maxAmount = compound.getInt("CurrentMaxAmount");
        caa$inStacksOrBuckets = compound.getBoolean("InStacksOrBuckets");
    }

    @Inject(method = "writeCommon", at = @At("TAIL"))
    private void writeCommonEx(CompoundTag compound, CallbackInfo ci) {
        compound.putBoolean("Precision", caa$precision);
        compound.putInt("OnAboveAmount", caa$onWhenAbove);
        compound.putInt("OffBelowAmount", caa$offWhenBelow);
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void writeEx(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        compound.putInt("CurrentAmount", caa$currentAmount);
        compound.putInt("CurrentMinAmount", caa$minAmount);
        compound.putInt("CurrentMaxAmount", caa$maxAmount);
        compound.putBoolean("InStacksOrBuckets", caa$inStacksOrBuckets);
    }

    @Inject(method = "updateCurrentLevel", at = @At("HEAD"), cancellable = true)
    private void updateCurrentLevelEx(CallbackInfo ci) {
        if (!caa$precision)
            return;
        ThresholdSwitchBlockEntity be = (ThresholdSwitchBlockEntity)(Object) this;
        Level level = be.getLevel();
        if (level == null)
            return;
        ci.cancel();

        int prevAmount = caa$currentAmount;
        int prevMaxAmount = caa$maxAmount;

        BlockPos target = caa$getTargetPos();
        BlockEntity targetBlockEntity = level.getBlockEntity(target);

        observedInventory.findNewCapability();
        observedTank.findNewCapability();

        if (targetBlockEntity instanceof ThresholdSwitchObservableEx observable) {
            caa$minAmount = observable.caa$getMinValue();
            caa$currentAmount = observable.caa$getCurrentValue();
            caa$maxAmount = observable.caa$getMaxValue();
        } else if (observedInventory.hasInventory() || observedTank.hasInventory()) {
            caa$minAmount = 0;
            caa$currentAmount = 0;
            caa$maxAmount = 0;

            if (observedInventory.hasInventory()) {
                // Item inventory
                IItemHandler inv = observedInventory.getInventory();
                if (invVersionTracker.stillWaiting(inv)) {
                    caa$currentAmount = prevAmount;
                    caa$maxAmount = prevMaxAmount;
                } else {
                    invVersionTracker.awaitNewVersion(inv);
                    // noinspection DataFlowIssue
                    for (int slot = 0; slot < inv.getSlots(); slot++) {
                        ItemStack stackInSlot = inv.getStackInSlot(slot);

                        int finalSlot = slot;
                        long space = COMPAT
                            .stream()
                            .filter(compat -> compat.isFromThisMod(targetBlockEntity))
                            .map(compat -> compat.getSpaceInSlot(inv, finalSlot))
                            .findFirst()
                            .orElseGet(() -> {
                                int maxStack = stackInSlot.isEmpty() ? 64 : stackInSlot.getMaxStackSize();
                                return (long) Math.min(maxStack, inv.getSlotLimit(finalSlot));
                            });

                        int count = stackInSlot.getCount();
                        if (space == 0)
                            continue;

                        caa$maxAmount += (int) space;
                        if (filtering.test(stackInSlot))
                            caa$currentAmount += count;
                    }
                }
            }
            if (observedTank.hasInventory()) {
                // Fluid inventory
                IFluidHandler tank = observedTank.getInventory();
                // noinspection DataFlowIssue
                for (int slot = 0; slot < tank.getTanks(); slot++) {
                    FluidStack stackInSlot = tank.getFluidInTank(slot);
                    int space = tank.getTankCapacity(slot);
                    int count = stackInSlot.getAmount();
                    if (space == 0)
                        continue;

                    caa$maxAmount += space;
                    if (filtering.test(stackInSlot))
                        caa$currentAmount += count;
                }
            }
        } else {
            // No compatible inventories found
            caa$minAmount = -1;
            caa$maxAmount = -1;
            if (caa$currentAmount == -1)
                return;

            level.setBlock(be.getBlockPos(), be.getBlockState().setValue(ThresholdSwitchBlock.LEVEL, 0), Block.UPDATE_ALL);
            caa$currentAmount = -1;
            redstoneState = false;
            be.sendData();
            scheduleBlockTick();
            return;
        }

        caa$currentAmount = Mth.clamp(caa$currentAmount, caa$minAmount, caa$maxAmount);
        boolean changed = caa$currentAmount != prevAmount;

        boolean previouslyPowered = redstoneState;
        if (redstoneState && caa$currentAmount <= caa$offWhenBelow)
            redstoneState = false;
        else if (!redstoneState && caa$currentAmount >= caa$onWhenAbove)
            redstoneState = true;
        boolean update = previouslyPowered != redstoneState;

        int displayLevel = 0;
        float normedLevel = (float) (caa$currentAmount - caa$minAmount) / (caa$maxAmount - caa$minAmount);
        if (caa$currentAmount > caa$minAmount)
            displayLevel = (int) (1 + normedLevel * 4);
        level.setBlock(be.getBlockPos(), be.getBlockState().setValue(ThresholdSwitchBlock.LEVEL, displayLevel),
            update ? 3 : 2);

        if (update)
            scheduleBlockTick();

        if (changed || update) {
            DisplayLinkBlock.notifyGatherers(level, be.getBlockPos());
            be.notifyUpdate();
        }
    }
}
