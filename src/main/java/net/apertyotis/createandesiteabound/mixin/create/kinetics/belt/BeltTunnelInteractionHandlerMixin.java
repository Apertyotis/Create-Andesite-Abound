package net.apertyotis.createandesiteabound.mixin.create.kinetics.belt;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.BeltTunnelInteractionHandler;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = BeltTunnelInteractionHandler.class, remap = false)
public abstract class BeltTunnelInteractionHandlerMixin {
    /**
     * 解决正向传送带上第一个安山隧道失效的问题<br>
     * 详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/9967">#9967</a>
     */
    @ModifyVariable(
            method = "flapTunnelsAndCheckIfStuck",
            at = @At(
                    value = "STORE"
            ),
            name = "currentSegment"
    )
    private static int redirectCurrentSegment(
            int currentSegment,
            @Local(argsOnly = true) BeltInventory beltInventory,
            @Local(argsOnly = true) TransportedItemStack current)
    {
        if (((BeltInventoryAccessor) beltInventory).isPositive() && current.beltPosition <= .0f)
            return -1;
        return currentSegment;
    }
}
