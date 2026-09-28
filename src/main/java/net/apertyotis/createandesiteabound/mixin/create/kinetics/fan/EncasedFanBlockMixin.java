package net.apertyotis.createandesiteabound.mixin.create.kinetics.fan;

import com.simibubi.create.content.kinetics.fan.EncasedFanBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EncasedFanBlock.class, remap = false)
public abstract class EncasedFanBlockMixin {
    @Inject(method = "hasShaftTowards", at = @At("HEAD"), cancellable = true)
    private void noShaft(LevelReader world, BlockPos pos, BlockState state, Direction face, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
