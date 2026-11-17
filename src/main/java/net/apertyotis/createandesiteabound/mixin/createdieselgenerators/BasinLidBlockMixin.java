package net.apertyotis.createandesiteabound.mixin.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.BasinLidBlock;
import com.jesz.createdieselgenerators.blocks.entity.BasinLidBlockEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BasinLidBlock.class)
public abstract class BasinLidBlockMixin {
    @ModifyExpressionValue(
        method = "use",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"
        )
    )
    private Comparable<?> scheduleUpdate(
        Comparable<?> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos
    ) {
        if ((Boolean) original && level.getBlockEntity(pos) instanceof BasinLidBlockEntity lid) {
            lid.basinChecker.scheduleUpdate();
        }
        return original;
    }
}
