package net.apertyotis.createandesiteabound.mixin.create.fluids.hosePulley;

import com.simibubi.create.content.fluids.transfer.FluidManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FluidManipulationBehaviour.class, remap = false)
public abstract class FluidManipulationBehaviourMixin extends BlockEntityBehaviour {
    @Unique
    private int caa$effectCooldown;

    public FluidManipulationBehaviourMixin(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public void tick() {
        super.tick();
        if (caa$effectCooldown > 0)
            caa$effectCooldown--;
    }

    @Inject(method = "playEffect", at = @At("HEAD"), cancellable = true)
    private void lazyEffect(Level world, BlockPos pos, Fluid fluid, boolean fillSound, CallbackInfo ci) {
        if (caa$effectCooldown > 0) {
            ci.cancel();
        } else {
            caa$effectCooldown = 10;
        }
    }
}
