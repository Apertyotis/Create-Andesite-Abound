package net.apertyotis.createandesiteabound.mixin.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.entity.BasinLidBlockEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.simple.DeferralBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BasinLidBlockEntity.class, remap = false)
public abstract class BasinLidBlockEntityMixin extends BasinOperatingBlockEntity {

    @Unique
    private int caa$cooldown = 0;

    public BasinLidBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public boolean continueWithPreviousRecipe() {
        startProcessingBasin();
        return true;
    }

    @ModifyExpressionValue(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/Level;isClientSide:Z",
            opcode = Opcodes.GETFIELD,
            ordinal = 0,
            remap = true
        )
    )
    private boolean searchRecipeOnClient(boolean original) {
        return false;
    }

    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/simple/DeferralBehaviour;scheduleUpdate()V"
        )
    )
    private void lazyChecker(DeferralBehaviour instance, Operation<Void> original) {
        if (--caa$cooldown <= 0) {
            caa$cooldown = 200;
            original.call(instance);
        }
    }

    @Inject(method = "startProcessingBasin", at = @At("TAIL"))
    private void onStartProcessing(CallbackInfo ci) {
        BasinLidBlockEntity self = (BasinLidBlockEntity)(Object) this;
        if (self.processingTime > 0)
            self.processingTime--;
    }
}
