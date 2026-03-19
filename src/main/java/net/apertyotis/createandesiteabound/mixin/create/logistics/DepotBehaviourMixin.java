package net.apertyotis.createandesiteabound.mixin.create.logistics;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.item.ItemHelper;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = DepotBehaviour.class, remap = false)
public abstract class DepotBehaviourMixin {

    @Shadow
    TransportedItemStack heldItem;

    @Shadow
    List<TransportedItemStack> incoming;

    @Shadow
    protected abstract boolean tick(TransportedItemStack heldItem);

    // 将物品运动到中心动画改为 5tick
    @Inject(method = "tick(Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void redirectAnimateTick(TransportedItemStack heldItem, CallbackInfoReturnable<Boolean> cir) {
        if (!AllConfig.depot_speed_change)
            return;

        heldItem.prevBeltPosition = heldItem.beltPosition;
        heldItem.prevSideOffset = heldItem.sideOffset;
        float diff = .5f - heldItem.beltPosition;
        if (diff > 1 / 512f) {
            if (diff > 1 / 32f && !BeltHelper.isItemUpright(heldItem.stack))
                heldItem.angle += 1;
            heldItem.beltPosition += diff / 2f;
        }
        cir.setReturnValue(diff < 1 / 16f);
    }

    // 重写置物台合并额外物品逻辑，防止掉落无法合并的物品
    @ModifyExpressionValue(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Iterator;hasNext()Z"
            )
    )
    private boolean preventDropping(boolean original) {
        if (!AllConfig.no_depot_overflow_drop)
            return original;

        DepotBehaviour behaviour = (DepotBehaviour)(Object) this;
        SmartBlockEntity blockEntity = behaviour.blockEntity;

        var it = incoming.iterator();
        while (it.hasNext()) {
            TransportedItemStack ts = it.next();
            if (!tick(ts))
                continue;
            if (behaviour.getWorld().isClientSide && !blockEntity.isVirtual())
                continue;
            if (heldItem == null) {
                heldItem = ts;
            } else if (ItemHelper.canItemStackAmountsStack(heldItem.stack, ts.stack)) {
                heldItem.stack.grow(ts.stack.getCount());
            } else {
                continue;
            }
            it.remove();
            blockEntity.notifyUpdate();
        }

        return false;
    }

    // 防止置物台溢出物品时执行加工配方
    @Inject(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/BlockEntityBehaviour;get(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lcom/simibubi/create/foundation/blockEntity/behaviour/BehaviourType;)Lcom/simibubi/create/foundation/blockEntity/behaviour/BlockEntityBehaviour;"
            ),
            cancellable = true
    )
    private void preventOverProcessing(CallbackInfo ci) {
        if (!AllConfig.no_depot_overflow_drop)
            return;
        if (!incoming.isEmpty())
            ci.cancel();
    }

    // 将置物台加工溢出的产物移到 incoming 列表
    @WrapOperation(
            method = "applyToAllItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"
            ),
            remap = true
    )
    private void moveOverflowItemsToIncoming(Level p_18993_, double p_18994_, double p_18995_, double p_18996_, ItemStack p_18997_, Operation<Void> original) {
        if (!AllConfig.no_depot_overflow_drop) {
            original.call(p_18993_, p_18994_, p_18995_, p_18996_, p_18997_);
            return;
        }

        if (p_18997_.isEmpty())
            return;

        incoming.add(new TransportedItemStack(p_18997_));
    }

    // 防止弹射置物台无限接受未合并物品
    @Inject(
            method = "insert",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/depot/DepotBehaviour;getRemainingSpace()I"
            ),
            cancellable = true
    )
    private void preventInfiniteIncoming(TransportedItemStack heldItem, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (!AllConfig.no_depot_overflow_drop)
            return;

        if (this.heldItem != null && this.incoming.size() >= 4)
            cir.setReturnValue(heldItem.stack);
    }

    // 让普通置物台能持久化保存额外物品 nbt
    @ModifyExpressionValue(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/depot/DepotBehaviour;canMergeItems()Z"
            )
    )
    private boolean writeIncoming(boolean original) {
        return true;
    }

    @ModifyExpressionValue(
            method = "read",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/depot/DepotBehaviour;canMergeItems()Z"
            )
    )
    private boolean readIncoming(boolean original) {
        return true;
    }
}
