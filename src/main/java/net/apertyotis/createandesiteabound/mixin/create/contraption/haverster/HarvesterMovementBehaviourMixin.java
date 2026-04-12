package net.apertyotis.createandesiteabound.mixin.create.contraption.haverster;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = HarvesterMovementBehaviour.class, remap = false)
public abstract class HarvesterMovementBehaviourMixin {

    // 收割机补种作物不再消耗种子
    @WrapOperation(
            method = "lambda$visitNewPosition$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V",
                    remap = true
            )
    )
    private void notConsumeSeed(ItemStack instance, int p_41775_, Operation<Void> original) {
        if (!AllConfig.harvester_not_consume_seed)
            original.call(instance, p_41775_);
    }
}
