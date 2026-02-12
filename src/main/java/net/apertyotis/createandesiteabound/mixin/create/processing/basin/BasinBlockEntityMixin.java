package net.apertyotis.createandesiteabound.mixin.create.processing.basin;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(value = BasinBlockEntity.class, remap = false)
public abstract class BasinBlockEntityMixin extends SmartBlockEntity {
    @Shadow
    private boolean contentsChanged;

    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public BasinBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 工作盆也会提醒头顶的工作方块更新（让锅盖类工作方块不必频繁主动搜索配方
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (level == null)
            return;
        if (!contentsChanged)
            return;

        BlockEntity be = level.getBlockEntity(worldPosition.above(1));
        if (be instanceof BasinOperatingBlockEntity boe) {
            Optional<BasinBlockEntity> basin = ((BasinOperatingBlockEntityAccessor) boe).invokeGetBasin();
            if (basin.isPresent() && basin.get().getBlockPos().equals(getBlockPos())) {
                boe.basinChecker.scheduleUpdate();
            }
        }
    }

    // 修复工作盆对1个空流体输出槽分别判断是否接受两种输出而导致吞流体的问题
    @Inject(method = "acceptFluidOutputsIntoBasin", at = @At("HEAD"), cancellable = true)
    private void redirectAcceptFluidOutputsIntoBasin(
            List<FluidStack> outputFluids,
            boolean simulate,
            IFluidHandler targetTank,
            CallbackInfoReturnable<Boolean> cir)
    {
        if (targetTank instanceof SmartFluidTankBehaviour.InternalFluidHandler internalFluidHandler) {
            // 只有 simulate 且输出多种流体时存在 bug
            if (!simulate || outputFluids.size() <= 1) return;

            // 尝试测试插入多流体，会记录被占用的槽位
            CombinedTankWrapperAccessor accessor = (CombinedTankWrapperAccessor) internalFluidHandler;
            IFluidHandler[] handlers = accessor.getItemHandler();
            boolean[] occupied = new boolean[handlers.length];
            for (FluidStack stack: outputFluids) {
                // 原样复制 CombinedTankWrapper 的 fill 方法，但是会记录成功注入流体的储罐，并在之后测试中认为这些储罐无法插入
                // 假如有配方存在多个同种流体输出则会判断失误，但目前应该不用担心这个，因为没有设定多个同种流体输出的必要，流体输出也没有概率设定
                if (stack.isEmpty()) continue;

                int filled = 0;
                FluidStack resource = stack.copy();

                boolean fittingHandlerFound = false;
                Outer: for (boolean searchPass : Iterate.trueAndFalse) {
                    for (int i = 0; i < handlers.length; i++) {
                        if (occupied[i]) continue;

                        IFluidHandler iFluidHandler = handlers[i];
                        for (int j = 0; j < iFluidHandler.getTanks(); j++)
                            if (searchPass && iFluidHandler.getFluidInTank(j)
                                    .isFluidEqual(resource))
                                fittingHandlerFound = true;

                        if (searchPass && !fittingHandlerFound)
                            continue;

                        int filledIntoCurrent = iFluidHandler.fill(resource, IFluidHandler.FluidAction.SIMULATE);
                        resource.shrink(filledIntoCurrent);
                        if (filledIntoCurrent != 0) {
                            occupied[i] = true;
                            filled += filledIntoCurrent;
                        }

                        if (resource.isEmpty())
                            break Outer;
                        if (fittingHandlerFound && (accessor.isEnforceVariety() || filledIntoCurrent != 0))
                            break Outer;
                    }
                }

                if (stack.getAmount() != filled) {
                    cir.setReturnValue(false);
                    return;
                }
            }

            cir.setReturnValue(true);
        } else {
            // 根据原方法语义，绝不应该传入其他类型的handler
            throw new IllegalArgumentException(
                    "[Create: Andesite Abound Mixin] Handler type contract violated: expected SmartFluidTankBehaviour.InternalFluidHandler, got "
                            + targetTank.getClass().getName()
            );
        }
    }
}
