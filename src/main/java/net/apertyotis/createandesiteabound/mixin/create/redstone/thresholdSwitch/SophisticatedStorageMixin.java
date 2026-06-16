package net.apertyotis.createandesiteabound.mixin.create.redstone.thresholdSwitch;

import com.simibubi.create.compat.thresholdSwitch.SophisticatedStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SophisticatedStorage.class, remap = false)
public abstract class SophisticatedStorageMixin {
    @Inject(method = "getSpaceInSlot", at = @At("HEAD"), cancellable = true)
    private void getCorrectSpaceInSlot(IItemHandler inv, int slot, CallbackInfoReturnable<Long> cir) {
        ItemStack stackIn = inv.getStackInSlot(slot);
        long maxStack = stackIn.isEmpty() ? 64 : stackIn.getMaxStackSize();
        cir.setReturnValue(inv.getSlotLimit(slot) * maxStack / 64);
    }
}
