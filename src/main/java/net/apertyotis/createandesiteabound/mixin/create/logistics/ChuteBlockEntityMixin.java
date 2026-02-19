package net.apertyotis.createandesiteabound.mixin.create.logistics;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.logistics.chute.AbstractChuteBlock;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import net.apertyotis.createandesiteabound.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChuteBlockEntity.class, remap = false)
public abstract class ChuteBlockEntityMixin extends SmartBlockEntity implements IHaveGoggleInformation {

    @Shadow
    ItemStack item;

    @Shadow
    LerpedFloat itemPosition;

    @Shadow
    private void handleInputFromAbove() {}

    @Shadow
    private void handleInputFromBelow() {}

    @Shadow
    @Contract()
    private boolean handleDownwardOutput(boolean simulate) {return false;}

    @Shadow
    @Contract()
    private boolean handleUpwardOutput(boolean simulate) {return false;}

    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public ChuteBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 让各种常规情况下的单倍速溜槽速度为 5tick 每次，并防止底部无开口的斜溜槽吸取下方容器内容
    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/chute/ChuteBlockEntity;tickAirStreams(F)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void redirectInputAndMovement(
            CallbackInfo ci,
            @Local(name = "clientSide") boolean clientSide,
            @Local(name = "itemMotion") float itemMotion)
    {
        if (!Config.chute_speed_change && !Config.no_chute_leaking) return;

        // 取消原抽取物品逻辑，改为不耗时操作
        if (!clientSide && item.isEmpty()) {
            if (itemMotion < 0)
                handleInputFromAbove();

            if (itemMotion > 0) {
                // 修复斜溜槽底部漏风，不让斜溜槽从下方容器抽取
                if (!Config.no_chute_leaking || AbstractChuteBlock.getChuteFacing(getBlockState()) == Direction.DOWN)
                    handleInputFromBelow();
            }

            if (!Config.chute_speed_change) {
                ci.cancel();
                return;
            }
        }


        if (!item.isEmpty()) {
            float nextOffset = itemPosition.getValue() + itemMotion;

            if (itemMotion < 0) {
                // 减少1tick移动耗时
                if (nextOffset < 0.01f) {
                    // 让物品在终点等待而不是中央来减少连续抽取耗时
                    if (!handleDownwardOutput(true))
                        nextOffset = 0;
                    else {
                        handleDownwardOutput(clientSide);
                        nextOffset = itemPosition.getValue();
                    }
                }
            } else if (itemMotion > 0) {
                // 同上
                if (nextOffset > 0.99f) {
                    if (!handleUpwardOutput(true))
                        nextOffset = 1;
                    else {
                        handleUpwardOutput(clientSide);
                        nextOffset = itemPosition.getValue();
                    }
                }
            }

            itemPosition.setValue(nextOffset);
        }

        ci.cancel();
    }

    // 修复斜溜槽底部漏风，不让斜溜槽输出到下方容器
    @ModifyExpressionValue(
            method = "handleDownwardOutput",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/util/LazyOptional;isPresent()Z",
                    ordinal = 1
            )
    )
    private boolean redirectOutputToCapBelow(boolean original, @Local(name = "direction") Direction direction) {
        if (!Config.no_chute_leaking) return original;

        if (direction != Direction.DOWN)
            return false;
        else
            return original;
    }
}
