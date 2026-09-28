package net.apertyotis.createandesiteabound.mixin.create.processing.basin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.processing.basin.BasinBlock;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BasinBlock.class, remap = false)
public abstract class BasinBlockMixin {
    @WrapOperation(
        method = "canOutputTo",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/behaviour/DirectBeltInputBehaviour;canInsertFromSide(Lnet/minecraft/core/Direction;)Z"
        )
    )
    private static boolean insertToStillBelt(DirectBeltInputBehaviour behaviour, Direction side, Operation<Boolean> original) {
        if (behaviour.blockEntity instanceof BeltBlockEntity belt) {
            return belt.getSpeed() == 0 || belt.getMovementFacing() != side.getOpposite();
        }
        return original.call(behaviour, side);
    }
}
