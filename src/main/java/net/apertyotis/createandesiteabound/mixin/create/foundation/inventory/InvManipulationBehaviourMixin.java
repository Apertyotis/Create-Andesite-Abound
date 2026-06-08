package net.apertyotis.createandesiteabound.mixin.create.foundation.inventory;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(value = InvManipulationBehaviour.class, remap = false)
public abstract class InvManipulationBehaviourMixin {
    /**
     * 取消漏斗取物时多余的一次判断（导致一次完整的取物需要判断三遍）<br>
     * 详见 Create PR<a href="https://github.com/Creators-of-Create/Create/pull/9706">#9706</a>
     */
    @WrapOperation(
            method = "extract(" +
                    "Lcom/simibubi/create/foundation/item/ItemHelper$ExtractionCountMode;" +
                    "ILjava/util/function/Predicate;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/item/ItemHelper;extract(" +
                            "Lnet/minecraftforge/items/IItemHandler;" +
                            "Ljava/util/function/Predicate;" +
                            "Lcom/simibubi/create/foundation/item/ItemHelper$ExtractionCountMode;IZ)" +
                            "Lnet/minecraft/world/item/ItemStack;",
                    ordinal = 0
            )
    )
    private ItemStack singleExtract(
            IItemHandler inventory,
            Predicate<ItemStack> test,
            ItemHelper.ExtractionCountMode mode,
            int amount, boolean simulate,
            Operation<ItemStack> original,
            @Local(name = "shouldSimulate") boolean shouldSimulate,
            @Cancellable CallbackInfoReturnable<ItemStack> cir
    ) {
        ItemStack stack = original.call(inventory, test, mode, amount, shouldSimulate);
        cir.setReturnValue(stack);
        return stack;
    }
}
