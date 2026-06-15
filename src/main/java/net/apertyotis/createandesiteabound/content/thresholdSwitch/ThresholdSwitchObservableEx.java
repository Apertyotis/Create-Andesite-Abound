package net.apertyotis.createandesiteabound.content.thresholdSwitch;

import net.minecraft.network.chat.MutableComponent;

public interface ThresholdSwitchObservableEx {
    int caa$getMaxValue();

    int caa$getMinValue();

    int caa$getCurrentValue();

    MutableComponent caa$format(int value);
}
