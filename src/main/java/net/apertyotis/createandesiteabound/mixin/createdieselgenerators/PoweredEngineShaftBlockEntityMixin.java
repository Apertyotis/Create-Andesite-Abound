package net.apertyotis.createandesiteabound.mixin.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.entity.PoweredEngineShaftBlockEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.utility.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = PoweredEngineShaftBlockEntity.class, remap = false)
public abstract class PoweredEngineShaftBlockEntityMixin extends GeneratingKineticBlockEntity {

    @Shadow
    int movementDirection;

    @Shadow
    public Map<BlockPos, Couple<Float>> engines;

    public PoweredEngineShaftBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void onlyUpdateWhenChanged1(BlockPos sourcePos, int direction, float stress, float speed, CallbackInfo ci) {
        ci.cancel();
        Couple<Float> engine = engines.get(sourcePos);
        if (engine == null) {
            engines.put(sourcePos, Couple.create(stress, speed));
        } else if (engine.getFirst() == stress && engine.getSecond() == speed) {
            return;
        } else {
            engine.setFirst(stress);
            engine.setSecond(speed);
        }

        float maxSpeed = 0;
        for (Couple<Float> e: engines.values()) {
            maxSpeed = Math.max(Math.abs(e.getSecond()), maxSpeed);
        }
        this.speed = maxSpeed;
        movementDirection = direction;
        reActivateSource = true;
    }

    @ModifyExpressionValue(
        method = "removeGenerator",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"
        )
    )
    private Object onlyUpdateWhenChanged2(Object original, @Cancellable CallbackInfo ci) {
        if (original == null)
            ci.cancel();
        return original;
    }
}
