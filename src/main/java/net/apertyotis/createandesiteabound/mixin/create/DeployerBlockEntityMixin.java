package net.apertyotis.createandesiteabound.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

@Mixin(value = DeployerBlockEntity.class, remap = false)
public abstract class DeployerBlockEntityMixin extends KineticBlockEntity {

    @Shadow
    protected int timer;

    @Shadow
    protected abstract int getTimerSpeed();

    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public DeployerBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    // 让机械手等待阶段倒计时结束，但发现目标为传送带等装配目标时不再什么都不做
    // 而是将计时器设为-1000，保留原逻辑的兼容性，同时也让传送带装配回调知道机械手可以结束等待了
    @Inject(method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity;timer:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 3,
                    shift = At.Shift.AFTER
            )
    )
    private void endWaitingState(CallbackInfo ci) {
        Direction facing = getBlockState().getValue(FACING);
        if (facing != Direction.DOWN) return;
        if (BlockEntityBehaviour.get(level, worldPosition.below(2), TransportedItemStackHandlerBehaviour.TYPE) != null) {
            timer = -1000;
        }
    }

    // 跳过原先的倒计时代码
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity;timer:I",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            )
    )
    private int skipReturnIfStateCanChange(DeployerBlockEntity instance, Operation<Integer> original, @Cancellable CallbackInfo ci) {
        timer -= getTimerSpeed();
        if (timer > 0) ci.cancel();
        return -1;
    }
}
