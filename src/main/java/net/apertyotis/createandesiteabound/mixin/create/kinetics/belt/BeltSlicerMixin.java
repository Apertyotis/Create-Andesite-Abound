package net.apertyotis.createandesiteabound.mixin.create.kinetics.belt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltSlicer;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.apertyotis.createandesiteabound.content.belt.BeltBlockEntityEx;
import net.apertyotis.createandesiteabound.content.belt.BeltScrollValueBehaviour;
import net.minecraft.world.item.DyeColor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(value = BeltSlicer.class, remap = false)
public abstract class BeltSlicerMixin {
    @WrapOperation(
        method = "useConnector",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;color:Ljava/util/Optional;",
            opcode = Opcodes.PUTFIELD
        )
    )
    private static void setTargetSpeed(
        BeltBlockEntity instance, Optional<DyeColor> value, Operation<Void> original,
        @Local(name = "controllerBE") BeltBlockEntity controllerBE
    ) {
        original.call(instance, value);
        // 应力变化期间速度被视为0，需要通过内部方法获取设置值
        ScrollValueBehaviour behaviour = controllerBE.getBehaviour(ScrollValueBehaviour.TYPE);
        if (behaviour instanceof BeltScrollValueBehaviour) {
            float targetSpeed = behaviour.getValue();
            ((BeltBlockEntityEx) instance).caa$setTargetSpeed((int) targetSpeed);
        }
    }
}
