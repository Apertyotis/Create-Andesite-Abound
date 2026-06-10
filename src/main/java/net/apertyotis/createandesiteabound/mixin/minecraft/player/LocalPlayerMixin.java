package net.apertyotis.createandesiteabound.mixin.minecraft.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LocalPlayer.class)
public class LocalPlayerMixin {
    @WrapOperation(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isAlwaysFlying()Z",
                    ordinal = 1
            )
    )
    private boolean keepFlyingOnGround(MultiPlayerGameMode instance, Operation<Boolean> original) {
        if (!AllConfig.keep_flying_on_ground)
            return original.call(instance);
        else
            return true;
    }
}
