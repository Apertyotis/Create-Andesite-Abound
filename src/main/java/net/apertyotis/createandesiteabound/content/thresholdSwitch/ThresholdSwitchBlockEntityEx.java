package net.apertyotis.createandesiteabound.content.thresholdSwitch;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public interface ThresholdSwitchBlockEntityEx {
    boolean caa$inStacksOrBuckets();
    boolean caa$isPrecision();
    int caa$getMinAmount();
    int caa$getCurrentAmount();
    int caa$getMaxAmount();
    int caa$getOnWhenAbove();
    int caa$getOffWhenBelow();

    void caa$setPrecision(boolean precision);
    void caa$configure(int offBelow, int onAbove, boolean invert, boolean inStacksOrBuckets);
    ThresholdType caa$getTypeOfCurrentTarget();
    ItemStack caa$getDisplayItemForScreen();
    MutableComponent caa$format(int value, boolean stacksOrBuckets);

    enum ThresholdType {
        UNSUPPORTED, ITEM, FLUID, CUSTOM
    }
}
