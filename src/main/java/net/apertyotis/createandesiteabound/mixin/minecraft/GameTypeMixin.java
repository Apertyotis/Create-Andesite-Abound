package net.apertyotis.createandesiteabound.mixin.minecraft;

import net.apertyotis.createandesiteabound.Config;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameType.class)
public abstract class GameTypeMixin {
    // 任何模式玩家可飞行
    @Inject(method = "updatePlayerAbilities", at = @At("TAIL"))
    private void alwaysAllowFly(Abilities p_46399_, CallbackInfo ci) {
        if (!Config.always_allow_flying) return;
        p_46399_.mayfly = true;
    }
}
