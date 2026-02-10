package net.apertyotis.createandesiteabound.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(value = PortableStorageInterfaceMovement.class, remap = false)
public abstract class PortableStorageInterfaceMovementMixin {
    @Shadow
    public abstract void cancelStall(MovementContext context);

    @Shadow
    public abstract void reset(MovementContext context);

    /**
     * 部分修复移动接口永久停住运动结构的问题<br>
     * 详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/8542">#8542</a>
     */
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;contains(Ljava/lang/String;)Z"
            ),
            remap = true
    )
    private boolean cancelStallWhenWorkingPosAbsent(CompoundTag instance, String p_128442_, Operation<Boolean> original,
                                @Local(argsOnly = true) MovementContext context)
    {
        boolean value = original.call(instance, p_128442_);
        if (!value && context.stall) {
            cancelStall(context);
        }
        return value;
    }

    /**
     * 进一步修复移动接口永久停住运动结构的问题<br>
     * 详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/9109">#9109</a>
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Optional;isPresent()Z"
            )
    )
    private boolean cancelStallWhenCurrentFacingAbsent(Optional<Direction> instance, Operation<Boolean> original,
                                  @Local(argsOnly = true) MovementContext context)
    {
        boolean value = original.call(instance);
        if (!value) {
            reset(context);
        }
        return value;
    }
}
