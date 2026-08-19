package net.apertyotis.createandesiteabound.content.fluids.filling;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.Components;
import net.apertyotis.createandesiteabound.AllConfig;
import net.apertyotis.createandesiteabound.mixin.create.foundation.blockEntity.FilteringBehaviourAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public class FillingAmountBehaviour extends FilteringBehaviour {
    public FillingAmountBehaviour(SmartBlockEntity be, ValueBoxTransform slot) {
        super(be, slot);
        count = 0;
        forFluids();
    }

    @Override
    public boolean isCountVisible() {
        return true;
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        int oldCount = count;
        boolean result = super.setFilter(stack);
        count = oldCount;
        return result;
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        nbt.put("Filter", getFilter().serializeNBT());
        nbt.putInt("Amount", count);
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        ((FilteringBehaviourAccessor) this).setFilterInner(FilterItemStack.of(nbt.getCompound("Filter")));
        count = nbt.getInt("Amount");
        upTo = count == 0;
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        ImmutableList<Component> rows = ImmutableList.of(
            Components.literal("mB"),
            Components.literal("B"));
        ValueSettingsFormatter formatter = new ValueSettingsFormatter(this::formatSettings);
        return new ValueSettingsBoard(Component.translatable("caa.filling_machine.target_amount"),
            250, 5, rows, formatter);
    }

    public MutableComponent formatSettings(ValueSettings vs) {
        if (vs.value() == 0) {
            return Component.literal("*");
        } else if (vs.row() == 0) {
            return Component.literal(String.valueOf(vs.value() * 4));
        } else {
            int bucket = AllConfig.fluid_vessel_capacity * vs.value() / 250;
            return Component.literal(bucket == 0 ? "*" : String.valueOf(bucket));
        }
    }

    @Override
    public void setValueSettings(Player player, ValueSettings vs, boolean ctrlHeld) {
        if (!vs.equals(getValueSettings()))
            playFeedbackSound(this);
        if (vs.row() == 0) {
            count = vs.value() * 4;
        } else {
            count = AllConfig.fluid_vessel_capacity * vs.value() / 250 * 1000;
        }
        upTo = count == 0;
    }

    @Override
    public ValueSettings getValueSettings() {
        if (count % 1000 == 0) {
            return new ValueSettings(1, count * 250 / (AllConfig.fluid_vessel_capacity * 1000) + 1);
        } else {
            return new ValueSettings(0, count / 4);
        }
    }
}
