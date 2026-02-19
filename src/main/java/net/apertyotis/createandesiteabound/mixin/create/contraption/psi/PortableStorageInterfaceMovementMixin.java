package net.apertyotis.createandesiteabound.mixin.create.contraption.psi;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PortableStorageInterfaceMovement.class, remap = false)
public abstract class PortableStorageInterfaceMovementMixin {
    @Shadow
    public abstract void cancelStall(MovementContext context);

    /**
     * 部分修复移动接口永久停住运动结构的问题<br>
     * 详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/8542">#8542</a>
     */
    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;contains(Ljava/lang/String;)Z"
            ),
            remap = true
    )
    private boolean cancelStallWhenWorkingPosAbsent(boolean value, @Local(argsOnly = true) MovementContext context) {
        if (!value && context.stall) {
            cancelStall(context);
        }
        return value;
    }

    /**
     * 进一步修复移动接口永久停住运动结构的问题<br>
     * 详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/9109">#9109</a>
     */
    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Optional;isPresent()Z"
            )
    )
    private boolean cancelStallWhenCurrentFacingAbsent(boolean value, @Local(argsOnly = true) MovementContext context) {
        if (!value && context.stall) {
            cancelStall(context);
        }
        return value;
    }
}
