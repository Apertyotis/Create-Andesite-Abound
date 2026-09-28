package net.apertyotis.createandesiteabound.mixin.create.logistics.tunnel;

import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelItemHandler;
import net.apertyotis.createandesiteabound.content.tunnel.BrassTunnelBlockEntityEx;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BrassTunnelItemHandler.class, remap = false)
public abstract class BrassTunnelItemHandlerMixin {
    @Shadow
    private BrassTunnelBlockEntity blockEntity;

    @Inject(
        method = "insertItem",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/logistics/tunnel/BrassTunnelBlockEntity;canTakeItems()Z"
        )
    )
    private void tryInputByFunnel(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (blockEntity instanceof BrassTunnelBlockEntityEx ex) {
            ex.caa$tryInputFromSide();
        }
    }
}
