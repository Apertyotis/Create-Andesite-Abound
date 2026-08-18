package net.apertyotis.createandesiteabound.mixin.create.foundation.blockEntity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import net.apertyotis.createandesiteabound.content.fluids.filling.FillingAmountBehaviour;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FilteringRenderer.class, remap = false)
public abstract class FilteringRendererMixin {
    @WrapOperation(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/filtering/FilteringBehaviour;count:I",
            opcode = Opcodes.GETFIELD
        )
    )
    private static int fillingAmountFormatter(FilteringBehaviour instance, Operation<Integer> original) {
        int count = original.call(instance);
        if (instance instanceof FillingAmountBehaviour)
            count = count % 1000 == 0 ? count / 1000 : count;
        return count;
    }
}
