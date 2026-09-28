package net.apertyotis.createandesiteabound.mixin.create.kinetics.belt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlock;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.IPartialSafeNBT;
import com.simibubi.create.foundation.utility.NBTHelper;
import net.apertyotis.createandesiteabound.AllConfig;
import net.apertyotis.createandesiteabound.content.belt.BeltBlockEntityEx;
import net.apertyotis.createandesiteabound.content.belt.BeltScrollValueBehaviour;
import net.apertyotis.createandesiteabound.content.belt.BeltValueBoxTransform;
import net.apertyotis.createandesiteabound.content.tunnel.BrassTunnelBlockEntityEx;
import net.apertyotis.createandesiteabound.mixin.create.kinetics.base.KineticBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = BeltBlockEntity.class, remap = false)
public abstract class BeltBlockEntityMixin extends KineticBlockEntity implements BeltBlockEntityEx, IPartialSafeNBT {
    @Unique
    public int caa$stopWhenChanged = 0;

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

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (caa$stopWhenChanged > 0)
            caa$stopWhenChanged--;
    }

    // 添加轮椅选项
    @Inject(method = "addBehaviours", at = @At("TAIL"))
    private void speedControlBehaviour(List<BlockEntityBehaviour> behaviours, CallbackInfo ci) {
        caa$targetSpeed = new BeltScrollValueBehaviour(
            Component.translatable("create.kinetics.speed_controller.rotation_speed"),
            this, new BeltValueBoxTransform());
        caa$targetSpeed.between(-256, 256);
        caa$targetSpeed.requiresWrench();
        caa$targetSpeed.onlyActiveWhen(() -> AllConfig.easy_belt);
        behaviours.add(caa$targetSpeed);
    }

    @Override
    public float getSpeed() {
        if (AllConfig.easy_belt && caa$stopWhenChanged != 0)
            return 0;
        int value = caa$targetSpeed == null || !AllConfig.easy_belt ? 0 :
            caa$targetSpeed.getValue();
        return value == 0 ? super.getSpeed() : value;
    }

    @Override
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        caa$stopWhenChanged = 2;
        networkDirty = false;
        this.capacity = maxStress;
        this.stress = currentStress;
        ((KineticBlockEntityAccessor) this).setNetworkSize(networkSize);
        boolean overStressed = maxStress < currentStress && IRotate.StressImpact.isEnabled();
        setChanged();

        if (overStressed != this.overStressed) {
            float prevSpeed = super.getSpeed();
            this.overStressed = overStressed;
            onSpeedChanged(prevSpeed);
            sendData();
        }
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        float kineticSpeed = super.getSpeed();
        boolean fromOrToZero = (previousSpeed == 0) != (kineticSpeed == 0);
        boolean directionSwap = !fromOrToZero && Math.signum(previousSpeed) != Math.signum(kineticSpeed);
        if (fromOrToZero || directionSwap)
            ((KineticBlockEntityAccessor) this).setFlickerTally(getFlickerScore() + 5);
        setChanged();
    }

    @Override
    public void removeSource() {
        caa$stopWhenChanged = 2;
        float prevSpeed = super.getSpeed();

        speed = 0;
        source = null;
        setNetwork(null);
        sequenceContext = null;

        onSpeedChanged(prevSpeed);
    }

    @Override
    public float caa$getKineticSpeed() {
        return super.getSpeed();
    }

    @Override
    public void caa$setTargetSpeed(int value) {
        if (caa$targetSpeed != null)
            caa$targetSpeed.value = value;
    }

    @Override
    public void writeSafe(CompoundTag compound) {
        super.writeSafe(compound);
        NBTHelper.writeEnum(compound, "Casing", ((BeltBlockEntity)(Object) this).casing);
    }

    @WrapOperation(
        method = "tryInsertingFromSide",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/logistics/tunnel/BrassTunnelBlockEntity;getStackToDistribute()Lnet/minecraft/world/item/ItemStack;"
        )
    )
    private ItemStack tryInputTunnelFromSide(
        BrassTunnelBlockEntity tunnel, Operation<ItemStack> original,
        @Local(argsOnly = true) TransportedItemStack inserted, @Local(argsOnly = true) Direction side,
        @Cancellable CallbackInfoReturnable<ItemStack> cir
    ) {
        ItemStack stack = original.call(tunnel);
        if (tunnel instanceof BrassTunnelBlockEntityEx ex) {
            Direction.Axis axis = tunnel.getBlockState().getValue(BrassTunnelBlock.HORIZONTAL_AXIS);
            if (side.getAxis() == axis) {
                if (!ex.caa$canInputFromBack())
                    cir.setReturnValue(inserted.stack);
            } else {
                ex.caa$tryInputFromSide();
            }
        }
        return stack;
    }
}
