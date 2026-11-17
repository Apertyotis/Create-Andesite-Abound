package net.apertyotis.createandesiteabound.mixin.suppementaries;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FaucetBlockTile.class, remap = false)
public abstract class FaucetBlockTileMixin {
    @Expression("? > 0")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean accurateCooldown(
        boolean ignored, @Local(argsOnly = true) FaucetBlockTile faucet,
        @Cancellable CallbackInfo ci
    ) {
        int cooldown = ((FaucetBlockTileAccessor) faucet).getCooldown();
        if (cooldown > 0) {
            cooldown--;
            ((FaucetBlockTileAccessor) faucet).setCooldown(cooldown);
            if (cooldown > 0)
                ci.cancel();
        }
        return false;
    }
}
