package net.apertyotis.createandesiteabound.mixin.create.fluids.pipes;

import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidValveBlockEntity.class, remap = false)
public abstract class FluidValveBlockEntityMixin {

    // 将256转速下的阀门开关动画时长改为 1tick
    @Inject(method = "getChaseSpeed", at = @At("HEAD"), cancellable = true)
    private void modifyChaseSpeed(CallbackInfoReturnable<Float> cir) {
        if (!AllConfig.valve_speed_change)
            return;

        cir.setReturnValue(Mth.clamp(Math.abs(((KineticBlockEntity)(Object) this).getSpeed()) / 256, 0, 1));
    }
}
