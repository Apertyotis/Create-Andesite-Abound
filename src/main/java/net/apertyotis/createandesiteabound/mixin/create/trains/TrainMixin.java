package net.apertyotis.createandesiteabound.mixin.create.trains;

import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


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
}
