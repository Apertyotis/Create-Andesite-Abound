package net.apertyotis.createandesiteabound.mixin.create.kinetics.drill;

import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.drill.DrillBlockEntity;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.apertyotis.createandesiteabound.content.drill.DrillItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DrillBlockEntity.class, remap = false)
public abstract class DrillBlockEntityMixin extends BlockBreakingKineticBlockEntity {

    @Unique
    private DrillItemHandler caa$inventory;

    @Unique
    private LazyOptional<IItemHandler> caa$capProvider;

    public DrillBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initInventory(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        caa$inventory = new DrillItemHandler(this);
        caa$capProvider = LazyOptional.of(() -> caa$inventory);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        caa$capProvider.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER)
            return caa$capProvider.cast();
        return super.getCapability(cap, side);
    }

    @Override
    protected boolean shouldRun() {
        return super.shouldRun() && caa$inventory.items.isEmpty();
    }

    @Override
    public void onBlockBroken(BlockState stateToBreak) {
        if (level == null || !caa$inventory.items.isEmpty())
            return;
        ChuteBlockEntity above = level.getBlockEntity(breakingPos.above()) instanceof ChuteBlockEntity chute &&
            chute.getItemMotion() > 0 ? chute : null;
        DirectBeltInputBehaviour below =
            BlockEntityBehaviour.get(level, breakingPos.below(), DirectBeltInputBehaviour.TYPE);
        BlockHelper.destroyBlock(level, breakingPos, 1f, (stack) -> {
            if (stack.isEmpty())
                return;
            if (level.restoringBlockSnapshots)
                return;
            if (above != null && above.getItem().isEmpty()) {
                above.setItem(stack);
            } else if (below != null && below.canInsertFromSide(Direction.UP)) {
                ItemStack remainder = below.handleInsertion(stack, Direction.UP, false);
                if (!remainder.isEmpty()) {
                    caa$inventory.items.add(remainder);
                    notifyUpdate();
                }
            } else {
                caa$inventory.items.add(stack);
                notifyUpdate();
            }
        });
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        ListTag list = new ListTag();
        for (ItemStack stack: caa$inventory.items) {
            if (!stack.isEmpty())
                list.add(stack.serializeNBT());
        }
        compound.put("Overflow", list);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        caa$inventory.items.clear();
        ListTag tagList = compound.getList("Overflow", Tag.TAG_COMPOUND);
        for (Tag tag: tagList) {
            ItemStack stack = ItemStack.of((CompoundTag) tag);
            if (!stack.isEmpty())
                caa$inventory.items.add(stack);
        }
    }
}
