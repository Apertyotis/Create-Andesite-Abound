package net.apertyotis.createandesiteabound.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Mixin(value = BeltInventory.class, remap = false)
public abstract class BeltInventoryMixin {

    @Final
    @Shadow
    private List<TransportedItemStack> items;

    @Shadow
    boolean beltMovementPositive;

    // 二分查找下界 (传送带上最远者)
    // 返回可能超出索引 (等于size)
    @Unique
    static private int caa$lowerBound(List<TransportedItemStack> items, float target, boolean positive) {
        int l = 0, r = items.size();
        while (l < r) {
            int m = (l + r) >>> 1;
            if (positive ? items.get(m).beltPosition > target : items.get(m).beltPosition < target) {
                l = m + 1;
            } else {
                r = m;
            }
        }
        return l;
    }

    // 取消原先缓慢的遍历判断，改用二分查找判断输入位置是否被阻塞
    @WrapOperation(
            method = "canInsertAtFromSide",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;iterator()Ljava/util/Iterator;",
                    ordinal = 0
            )
    )
    private Iterator<Object> hasItemBlocking(
            List<TransportedItemStack> items,
            Operation<Iterator<?>> original,
            @Local(argsOnly = true) int segment,
            @Local(argsOnly = true) Direction side,
            @Local(name = "segmentPos") float segmentPos,
            @Cancellable CallbackInfoReturnable<Boolean> cir)
    {
        // 所需判断的区间最远处
        float furtherPos = beltMovementPositive ? segmentPos + 1 : segmentPos - 1;
        // 最近处为中点回退1/16格，这是插入物品的位置
        float closerPos = segment + 0.5f + (beltMovementPositive ? -1f : 1f) / 16;
        // 约定最远物品在列表低索引处
        int index = caa$lowerBound(items, furtherPos, beltMovementPositive);
        while(index < items.size()) {
            TransportedItemStack stack = items.get(index);
            if (beltMovementPositive ? stack.beltPosition < closerPos : stack.beltPosition > closerPos) {
                // 区间遍历完成，退出
                break;
            }
            if (stack.insertedAt == segment && stack.insertedFrom == side) {
                // 这个方向向这格插入的物品仍未离开判定区域，判定为这个方向被占用
                cir.setReturnValue(false);
                break;
            }
            ++index;
        }
        // 返回空迭代器，从而禁用原先缓慢的遍历逻辑
        return Collections.emptyIterator();
    }

    // 二分查找插入
    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void redirectInsert(TransportedItemStack newStack, CallbackInfo ci) {
        // 找到传送带上与插入物品堆位置相同或更靠起始端的物品堆索引，结果有可能是列表末尾+1
        int index = caa$lowerBound(items, newStack.beltPosition, beltMovementPositive);
        // 原代码逻辑会将物品插在已有相同beltPosition物品堆之后，这里需要保持行为一致
        if (index < items.size() && items.get(index).beltPosition == newStack.beltPosition) {
            ++index;
        }
        // 插入元素
        items.add(index, newStack);
        // 取消原方法
        ci.cancel();
    }

    // 二分查找取元素
    @Inject(method = "getStackAtOffset", at = @At("HEAD"), cancellable = true)
    private void redirectGetStackAtOffset(int offset, CallbackInfoReturnable<TransportedItemStack> cir) {
        // 计算上下界位置
        float furtherPos = offset;
        float closerPos = offset;
        if (beltMovementPositive) {
            furtherPos += 1;
        } else {
            closerPos += 1;
        }

        // 靠终点端物品会优先提取，与原方法行为一致
        int index = caa$lowerBound(items, furtherPos, beltMovementPositive);
        if (index < items.size()) {
            TransportedItemStack stack = items.get(index);
            if (beltMovementPositive ? stack.beltPosition >= closerPos : stack.beltPosition <= closerPos) {
                // 物品有效，返回
                cir.setReturnValue(stack);
                return;
            }
        }

        // 未找到物品，返回空，取消原方法
        cir.setReturnValue(null);
    }
}
