package net.apertyotis.createandesiteabound.mixin.create.fluids;

import com.simibubi.create.content.fluids.FlowSource;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.SmartFluidPipeBlockEntity;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlock;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlockEntity;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;
import net.apertyotis.createandesiteabound.foundation.FluidTransportBehaviourEx;
import net.apertyotis.createandesiteabound.mixin.create.foundation.blockEntity.FilteringBehaviourAccessor;
import net.apertyotis.createandesiteabound.mixin.create.logistics.FilterItemStackAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(value = FlowSource.FluidHandler.class, remap = false)
public abstract class FluidHandlerSourceMixin extends FlowSource {

    @Unique
    Level caa$level;
    @Unique
    boolean caa$resolved = false;
    @Unique
    FluidStack caa$resolvedFilter = null;

    public FluidHandlerSourceMixin(BlockFace location) {
        super(location);
    }

    @Inject(method = "manageSource", at = @At("HEAD"))
    private void onManageSource(Level level, CallbackInfo ci) {
        caa$level = level;
    }

    @Override
    public FluidStack provideFluid(Predicate<FluidStack> extractionPredicate) {
        IFluidHandler handler = provideHandler().resolve().orElse(null);
        if (handler == null || caa$level == null)
            return FluidStack.EMPTY;

        // 当智能流体管道更改过滤、流体阀门开关时，会触发流体网络更新，
        // 对于 FlowSource.FluidHandler 类型，旧对象会在更新后被抛弃，因此只需解析一遍
        if (!caa$resolved) {
            caa$resolved = true;
            BlockFace location = ((FlowSourceAccessor) this).getLocation();
            BlockEntity be = caa$level.getBlockEntity(location.getPos());

            if (be instanceof SmartFluidPipeBlockEntity smartPipe) {
                caa$resolvedFilter = caa$resolveFilter(caa$level, smartPipe);
            } else if (be instanceof FluidValveBlockEntity valve) {
                if (!valve.getBlockState().getValue(FluidValveBlock.ENABLED))
                    caa$resolvedFilter = FluidStack.EMPTY;
            } else if (be instanceof SmartBlockEntity sbe) {
                caa$resolvedFilter = caa$resolveFilterOtherPipe(caa$level, sbe);
            }
        }

        if (caa$resolvedFilter != null) {
            if (caa$resolvedFilter.isEmpty())
                return FluidStack.EMPTY;
            else
                return handler.drain(caa$resolvedFilter, FluidAction.SIMULATE);
        }
        return super.provideFluid(extractionPredicate);
    }

    @Unique
    private static FluidStack caa$resolveFilterOtherPipe(Level level, SmartBlockEntity sbe) {
        FluidTransportBehaviour pipe = sbe.getBehaviour(FluidTransportBehaviour.TYPE);
        if (pipe != null) {
            BlockPos pos = ((FluidTransportBehaviourEx) pipe).caa$getFilterPos();
            if (pos == null)
                return null;
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SmartFluidPipeBlockEntity smartPipe) {
                return caa$resolveFilter(level, smartPipe);
            } else if (be instanceof FluidValveBlockEntity valve) {
                if (!valve.getBlockState().getValue(FluidValveBlock.ENABLED))
                    return FluidStack.EMPTY;
            }
        }
        return null;
    }

    @Unique
    private static FluidStack caa$resolveFilter(Level level, SmartFluidPipeBlockEntity smartPipe) {
        FilteringBehaviour filter = smartPipe.getBehaviour(FilteringBehaviour.TYPE);
        if (filter != null) {
            FilterItemStack filterItem = ((FilteringBehaviourAccessor) filter).getFilterItemStack();
            if (filterItem.isEmpty() || filterItem.getClass() != FilterItemStack.class)
                return null;
            FilterItemStackAccessor accessor = (FilterItemStackAccessor) filterItem;
            accessor.invokeResolveFluid(level);
            FluidStack fluidStack = accessor.getFilterFluidStack().copy();
            if (fluidStack.isEmpty())
                return FluidStack.EMPTY;
            fluidStack.setAmount(1);
            return fluidStack;
        }
        return null;
    }
}
