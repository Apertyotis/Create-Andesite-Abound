package net.apertyotis.createandesiteabound.mixin.create.contraption.pulley;

import com.simibubi.create.content.contraptions.pulley.PulleyBlockEntity;
import net.apertyotis.createandesiteabound.content.thresholdSwitch.ThresholdSwitchObservableEx;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = PulleyBlockEntity.class, remap = false)
public abstract class PulleyBlockEntityMixin implements ThresholdSwitchObservableEx {

    @Shadow
    public abstract float getInterpolatedOffset(float partialTicks);

    @Unique
    @Override
    public int caa$getCurrentValue() {
        return ((BlockEntity)(Object) this).getBlockPos().getY() - (int) getInterpolatedOffset(.5f);
    }

    @Unique
    @Override
    public int caa$getMinValue() {
        Level level = ((BlockEntity)(Object) this).getLevel();
        if (level == null)
            return 0;
        return level.getMinBuildHeight();
    }

    @Unique
    @Override
    public int caa$getMaxValue() {
        return ((BlockEntity)(Object) this).getBlockPos().getY();
    }

    @Unique
    @Override
    public MutableComponent caa$format(int value) {
        return Component.translatable("caa.gui.threshold.pulley_y_level", value);
    }
}
