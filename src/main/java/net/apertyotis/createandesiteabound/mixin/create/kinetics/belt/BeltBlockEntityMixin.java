package net.apertyotis.createandesiteabound.mixin.create.kinetics.belt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.apertyotis.createandesiteabound.content.belt.BeltBlockEntityEx;
import net.apertyotis.createandesiteabound.content.belt.BeltScrollValueBehaviour;
import net.apertyotis.createandesiteabound.content.belt.BeltValueBoxTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = BeltBlockEntity.class, remap = false)
public class BeltBlockEntityMixin extends KineticBlockEntity implements BeltBlockEntityEx {
    @Unique
    public boolean caa$markDirty;

    @Unique
    public ScrollValueBehaviour caa$targetSpeed;

    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public BeltBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void sendData() {
        super.sendData();
        caa$markDirty = true;
    }

    /**
     * 部分修复传送带刷物品问题，详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/8335">#8335</a>
     */
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/belt/transport/BeltInventory;tick()V"
            )
    )
    private void beltInventoryTickWrapper(BeltInventory instance, Operation<Void> original) {
        caa$markDirty =false;
        original.call(instance);
        if (caa$markDirty) {
            setChanged();
        }
    }

    // 添加轮椅选项
    @Inject(method = "addBehaviours", at = @At("TAIL"))
    private void speedControlBehaviour(List<BlockEntityBehaviour> behaviours, CallbackInfo ci) {
        caa$targetSpeed = new BeltScrollValueBehaviour(
            Component.translatable("create.kinetics.speed_controller.rotation_speed"),
            this, new BeltValueBoxTransform());
        caa$targetSpeed.between(-256, 256);
        caa$targetSpeed.requiresWrench();
        behaviours.add(caa$targetSpeed);
    }

    @Override
    public float caa$getTargetSpeed() {
        int value = caa$targetSpeed == null ? 0 : caa$targetSpeed.getValue();
        return value == 0 ? getSpeed() : value;
    }

    @Override
    public void caa$setTargetSpeed(int value) {
        if (caa$targetSpeed != null)
            caa$targetSpeed.value = value;
    }

    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;getSpeed()F"
        )
    )
    private float redirectGetSpeed1(BeltBlockEntity instance, Operation<Float> original) {
        return caa$getTargetSpeed();
    }

    @WrapOperation(
        method = "getBeltMovementSpeed",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;getSpeed()F"
        )
    )
    private float redirectGetSpeed2(BeltBlockEntity instance, Operation<Float> original) {
        return caa$getTargetSpeed();
    }

    @WrapOperation(
        method = "getMovementDirection(ZZ)Lnet/minecraft/core/Vec3i;",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;getSpeed()F"
        )
    )
    private float redirectGetSpeed3(BeltBlockEntity instance, Operation<Float> original) {
        return caa$getTargetSpeed();
    }

    @WrapOperation(
        method = "canInsertFrom",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;getSpeed()F"
        )
    )
    private float redirectGetSpeed4(BeltBlockEntity instance, Operation<Float> original) {
        return caa$getTargetSpeed();
    }

    @WrapOperation(
        method = "isOccupied",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;getSpeed()F"
        )
    )
    private float redirectGetSpeed5(BeltBlockEntity instance, Operation<Float> original) {
        return caa$getTargetSpeed();
    }
}
