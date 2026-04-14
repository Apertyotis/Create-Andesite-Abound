package net.apertyotis.createandesiteabound.mixin.minecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Player.class)
public class PlayerMixin {
    // 禁用水中挖掘速度惩罚
    @WrapOperation(
            method = "getDigSpeed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"
            )
    )
    private boolean alwaysInAir(Player instance, TagKey<Fluid> tagKey, Operation<Boolean> original) {
        if (!AllConfig.disable_dig_speed_penalty)
            return original.call(instance, tagKey);
        return false;
    }

    // 禁用空中挖掘速度惩罚
    @WrapOperation(
            method = "getDigSpeed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;onGround()Z"
            )
    )
    private boolean alwaysOnGround(Player instance, Operation<Boolean> original) {
        if (!AllConfig.disable_dig_speed_penalty)
            return original.call(instance);
        return true;
    }
}
