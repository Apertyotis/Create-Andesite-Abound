package net.apertyotis.createandesiteabound.mixin.create.trains;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.signal.SignalEdgeGroup;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;


@Mixin(value = Train.class, remap = false)
public abstract class TrainMixin {
    @Inject(
            method = "lambda$frontSignalListener$6",
            at = @At("HEAD")
    )
    private void onFrontSignal(Double distance, Pair<TrackEdgePoint, Couple<TrackNode>> couple, CallbackInfoReturnable<Boolean> cir) {
        if (!(couple.getFirst() instanceof SignalBoundary signal))
            return;

        Train train = (Train)(Object) this;
        if (train.navigation.waitingForSignal != null && train.navigation.waitingForSignal.getFirst()
                .equals(signal.getId())) {
            if (train.reservedSignalBlocks.isEmpty())
                return;

            // 列车已预定区段，但意外遇到红灯，此时应该释放预留区段锁
            train.reservedSignalBlocks.clear();
        }
    }

    // 仅在当前信号区段范围内判定碰撞
    @WrapOperation(
        method = "findCollidingTrain",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"
        )
    )
    private Iterator<Train> getSignalGroupTrains(Collection<Train> instance, Operation<Iterator<Train>> original) {
        Set<Train> trains = new HashSet<>();
        for (UUID id: ((Train)(Object) this).occupiedSignalBlocks.keySet()) {
            SignalEdgeGroup group = Create.RAILWAYS.signalEdgeGroups.get(id);
            if (group != null) {
                trains.addAll(group.trains);
            }
        }
        return trains.iterator();
    }
}
