package net.apertyotis.createandesiteabound.mixin.create.redstone.thresholdSwitch;

import com.simibubi.create.content.redstone.thresholdSwitch.ConfigureThresholdSwitchPacket;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import net.apertyotis.createandesiteabound.content.thresholdSwitch.ThresholdSwitchBlockEntityEx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ConfigureThresholdSwitchPacket.class, remap = false)
public abstract class ConfigureThresholdSwitchPacketMixin {
    @Inject(method = "applySettings(Lcom/simibubi/create/content/redstone/thresholdSwitch/ThresholdSwitchBlockEntity;)V", at = @At("TAIL"))
    private void exitPrecisionMode(ThresholdSwitchBlockEntity be, CallbackInfo ci) {
        ((ThresholdSwitchBlockEntityEx) be).caa$setPrecision(false);
    }
}
