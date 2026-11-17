package net.apertyotis.createandesiteabound.mixin.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.entity.HugeDieselEngineBlockEntity;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = HugeDieselEngineBlockEntity.class, remap = false)
public abstract class HugeDieselEngineBlockEntityMixin {
    @WrapOperation(
        method = "write",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/fluid/SmartFluidTankBehaviour;write(Lnet/minecraft/nbt/CompoundTag;Z)V"
        )
    )
    private void redundantWrite(SmartFluidTankBehaviour instance, CompoundTag nbt, boolean clientPacket, Operation<Void> original) {}

    @WrapOperation(
        method = "read",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/fluid/SmartFluidTankBehaviour;read(Lnet/minecraft/nbt/CompoundTag;Z)V"
        )
    )
    private void redundantRead(SmartFluidTankBehaviour instance, CompoundTag nbt, boolean clientPacket, Operation<Void> original) {}

    @Expression("? == null")
    @ModifyExpressionValue(method = "getCapability", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean ignoreSide(boolean original) {
        return true;
    }

    @WrapOperation(
        method = "getShaft",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            ordinal = 0,
            remap = true
        )
    )
    private BlockState dontConvertShaft1(Level instance, BlockPos blockPos, Operation<BlockState> original) {
        return null;
    }

    @WrapOperation(
        method = "getShaft",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;",
            remap = true
        )
    )
    private Block dontConvertShaft2(BlockState instance, Operation<Block> original) {
        return null;
    }
}
