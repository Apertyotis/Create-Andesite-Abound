package net.apertyotis.createandesiteabound.mixin.minecraft.items;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ItemStack.class)
public abstract class ItemStackMixin {
    @SuppressWarnings("UnstableApiUsage")
    @WrapOperation(
            method = "isSameItemSameTags",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;areCapsCompatible(" +
                            "Lnet/minecraftforge/common/capabilities/CapabilityProvider;)Z",
                    remap = false
            )
    )
    private static boolean cancelItemCapCompare(
            ItemStack instance,
            CapabilityProvider<ItemStack> capabilityProvider,
            Operation<Boolean> original
    ) {
        if (AllConfig.dont_compare_item_capability)
            return true;
        else
            return original.call(instance, capabilityProvider);
    }
}
