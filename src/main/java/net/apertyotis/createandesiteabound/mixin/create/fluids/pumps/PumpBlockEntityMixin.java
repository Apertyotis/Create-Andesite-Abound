package net.apertyotis.createandesiteabound.mixin.create.fluids.pumps;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.SmartFluidPipeBlockEntity;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlockEntity;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Pair;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.apertyotis.createandesiteabound.foundation.BlockFaceEx;
import net.apertyotis.createandesiteabound.foundation.FluidTransportBehaviourEx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;

@Mixin(value = PumpBlockEntity.class, remap = false)
public abstract class PumpBlockEntityMixin {
    @WrapOperation(
        method = "distributePressureTo",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/fluids/FluidPropagator;getPipe(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lcom/simibubi/create/content/fluids/FluidTransportBehaviour;",
            ordinal = 0
        )
    )
    private FluidTransportBehaviour resetFilterPos(BlockGetter reader, BlockPos pos, Operation<FluidTransportBehaviour> original) {
        FluidTransportBehaviour pipeBehaviour = original.call(reader, pos);
        if (pipeBehaviour instanceof FluidTransportBehaviourEx ex)
            ex.caa$resetFilterPos();
        return pipeBehaviour;
    }

    // 记录 DFS 路径上最后遇到的过滤管道
    @Inject(method = "searchForEndpointRecursively", at = @At("HEAD"))
    private void initFilterPos(
        Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph, Set<BlockFace> targets,
        Map<Integer, Set<BlockFace>> validFaces, BlockFace currentFace, boolean pull,
        CallbackInfoReturnable<Boolean> cir, @Share("filterPos") LocalRef<BlockPos> filterPos
    ) {
        if (!pull)
            return;
        Level level = ((PumpBlockEntity)(Object) this).getLevel();
        filterPos.set(((BlockFaceEx) currentFace).caa$getFilterPos());
        if (level != null) {
            BlockEntity entity = level.getBlockEntity(currentFace.getPos());
            if (entity instanceof SmartFluidPipeBlockEntity || entity instanceof FluidValveBlockEntity) {
                filterPos.set(currentFace.getPos());
            }
        }
    }

    // 利用递归中传递的 BlockFace 对象，顺带传递过滤管道位置信息
    @WrapOperation(
        method = "searchForEndpointRecursively",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Lcom/simibubi/create/foundation/utility/BlockFace;",
            ordinal = 1
        )
    )
    private BlockFace propagateFilterPos(
        BlockPos first, Direction second, Operation<BlockFace> original,
        @Share("filterPos") LocalRef<BlockPos> filterPos
    ) {
        BlockFace face = original.call(first, second);
        ((BlockFaceEx) face).caa$withFilterPos(filterPos.get());
        return face;
    }

    // 将过滤管道位置附加到终点上
    @WrapOperation(
        method = "searchForEndpointRecursively",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"
        )
    )
    private boolean attachFilterPos(
        Set<BlockFace> targets, Object o, Operation<Boolean> original,
        @Share("filterPos") LocalRef<BlockPos> filterPos
    ) {
        boolean result = original.call(targets, o);
        Level level = ((PumpBlockEntity)(Object) this).getLevel();
        if (level != null && result) {
            FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(level, ((BlockFace) o).getPos());
            if (pipeBehaviour instanceof FluidTransportBehaviourEx ex)
                ex.caa$attachFilterPos(filterPos.get());
        }
        return result;
    }

    @Inject(method = "distributePressureTo", at = @At(value = "NEW", target = "()Ljava/util/HashMap;", ordinal = 0))
    private void initBorderNodes(Direction side, CallbackInfo ci, @Share("borderNodes") LocalRef<LongOpenHashSet> borderNodes) {
        borderNodes.set(new LongOpenHashSet());
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @Definition(id = "isLoaded", method = "Lnet/minecraft/world/level/Level;isLoaded(Lnet/minecraft/core/BlockPos;)Z", remap = true)
    @Expression("?.isLoaded(?)")
    @ModifyExpressionValue(method = "distributePressureTo", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 1))
    private boolean collectBorderNodes(
        boolean original, @Share("borderNodes") LocalRef<LongOpenHashSet> borderNodes,
        @Local(name = "currentPos") BlockPos currentPos
    ) {
        if (!original)
            borderNodes.get().add(currentPos.asLong());
        return original;
    }

    @Inject(method = "distributePressureTo", at = @At(value = "INVOKE", target = "Ljava/util/HashMap;<init>()V", ordinal = 1))
    private void markNeedsUpdate(Direction side, CallbackInfo ci, @Share("borderNodes") LocalRef<LongOpenHashSet> borderNodes) {
        Level level = ((PumpBlockEntity)(Object) this).getLevel();
        if (level == null)
            return;
        for (long packed: borderNodes.get()) {
            if (FluidPropagator.getPipe(level, BlockPos.of(packed)) instanceof FluidTransportBehaviourEx ex) {
                ex.caa$scheduleUpdate();
            }
        }
    }
}
