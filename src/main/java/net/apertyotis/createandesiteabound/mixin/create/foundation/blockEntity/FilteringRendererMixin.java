package net.apertyotis.createandesiteabound.mixin.create.foundation.blockEntity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsClient;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import net.apertyotis.createandesiteabound.content.fluids.filling.FillingAmountBehaviour;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

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

    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueSettingsClient;showHoverTip(Ljava/util/List;)V"
        )
    )
    private static void addEasyFilteringTooltip(
        ValueSettingsClient instance, List<MutableComponent> tip, Operation<Void> original
    ) {
        tip.add(Component.translatable("caa.easy_filter.hold_to_open"));
        original.call(instance, tip);
    }
}
