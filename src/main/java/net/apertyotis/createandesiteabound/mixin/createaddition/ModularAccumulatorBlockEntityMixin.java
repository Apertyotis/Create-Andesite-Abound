package net.apertyotis.createandesiteabound.mixin.createaddition;

import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorBlockEntity;
import net.apertyotis.createandesiteabound.content.thresholdSwitch.ThresholdSwitchObservableEx;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ModularAccumulatorBlockEntity.class, remap = false)
public abstract class ModularAccumulatorBlockEntityMixin implements ThresholdSwitchObservableEx {
    @Unique
    @Override
    public int caa$getMaxValue() {
        return 100;
    }

    @Unique
    @Override
    public int caa$getMinValue() {
        return 0;
    }

    @Unique
    @Override
    public int caa$getCurrentValue() {
        ModularAccumulatorBlockEntity controllerBE = ((ModularAccumulatorBlockEntity)(Object) this).getControllerBE();
        if (controllerBE == null)
            return 0;
        return (int) (100f * controllerBE.getEnergy().getEnergyStored() / controllerBE.getEnergy().getMaxEnergyStored());
    }

    @Unique
    @Override
    public MutableComponent caa$format(int i) {
        return Component.literal(i + "%");
    }
}
