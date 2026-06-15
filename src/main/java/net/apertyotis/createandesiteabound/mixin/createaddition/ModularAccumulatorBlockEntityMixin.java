package net.apertyotis.createandesiteabound.mixin.createaddition;

import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorBlockEntity;
import net.apertyotis.createandesiteabound.content.thresholdSwitch.ThresholdSwitchObservableEx;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ModularAccumulatorBlockEntity.class, remap = false)
public abstract class ModularAccumulatorBlockEntityMixin implements ThresholdSwitchObservableEx {
    @Override
    public int caa$getMaxValue() {
        return 100;
    }

    @Override
    public int caa$getMinValue() {
        return 0;
    }

    @Override
    public int caa$getCurrentValue() {
        ModularAccumulatorBlockEntity controllerBE = ((ModularAccumulatorBlockEntity)(Object) this).getControllerBE();
        if (controllerBE == null)
            return 0;
        return (int) (100f * controllerBE.getEnergy().getEnergyStored() / controllerBE.getEnergy().getMaxEnergyStored());
    }

    @Override
    public MutableComponent caa$format(int i) {
        return Component.literal(i + "%");
    }
}
