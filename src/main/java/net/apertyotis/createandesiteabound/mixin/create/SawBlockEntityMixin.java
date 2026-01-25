package net.apertyotis.createandesiteabound.mixin.create;

import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import net.apertyotis.createandesiteabound.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = SawBlockEntity.class, remap = false)
public abstract class SawBlockEntityMixin{

    // 配方时间固定为80，实际最快需要10tick处理
    @ModifyVariable(
            method = "start",
            at = @At(value = "LOAD"),
            name = "time"
    )
    private int setDefaultTime(int time) {
        if (!Config.saw_speed_change) return time;
        return 80;
    }
}
