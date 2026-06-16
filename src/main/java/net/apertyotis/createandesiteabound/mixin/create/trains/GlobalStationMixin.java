package net.apertyotis.createandesiteabound.mixin.create.trains;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.station.GlobalStation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GlobalStation.class, remap = false)
public abstract class GlobalStationMixin {
    /**
     * 修复某种情况下列车永久预定车站而不释放的问题<br>
     * 详见 Create PR<a href="https://github.com/Creators-of-Create/Create/pull/9875">#9875</a>
     */
    @WrapOperation(
        method = "reserveFor",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/trains/station/GlobalStation;getNearestTrain()Lcom/simibubi/create/content/trains/entity/Train;"
        )
    )
    private Train validateTrain(
        GlobalStation station, Operation<Train> original,
        @Local(argsOnly = true) Train train, @Cancellable CallbackInfo ci
    ) {
        if (train == null) {
            ci.cancel();
            return null;
        }
        Train nearestTrain = original.call(station);
        if (nearestTrain != null && nearestTrain.getCurrentStation() != station)
            nearestTrain = null;
        return nearestTrain;
    }
}
