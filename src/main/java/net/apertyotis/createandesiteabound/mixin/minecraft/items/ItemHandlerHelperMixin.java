package net.apertyotis.createandesiteabound.mixin.minecraft.items;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ItemHandlerHelper.class, remap = false)
public abstract class ItemHandlerHelperMixin {
    // 防止 forge 在比较两个物品堆叠是否相同时初始化物品 capability
    // 既为了避免传送带吞吐物品时对大量新物品无意义地初始化能力
    // 也是因为作为纯函数，比较物品不应该出现创建能力的副作用
    @SuppressWarnings("UnstableApiUsage")
    @WrapOperation(
            method = "canItemStacksStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;areCapsCompatible(" +
                            "Lnet/minecraftforge/common/capabilities/CapabilityProvider;)Z"
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

    @SuppressWarnings("UnstableApiUsage")
    @WrapOperation(
            method = "canItemStacksStackRelaxed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;areCapsCompatible(" +
                            "Lnet/minecraftforge/common/capabilities/CapabilityProvider;)Z"
            )
    )
    private static boolean cancelItemCapCompare2(
            ItemStack instance,
            CapabilityProvider<ItemStack> capabilityProvider,
            Operation<Boolean> original)
    {
        if (AllConfig.dont_compare_item_capability)
            return true;
        else
            return original.call(instance, capabilityProvider);
    }
}
