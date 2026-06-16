package net.apertyotis.createandesiteabound.mixin.create.trains;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.trains.entity.Navigation;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DiscoveredPath;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = Navigation.class, remap = false)
public abstract class NavigationMixin {
    @Shadow
    public GlobalStation destination;

    @Shadow
    public Train train;

    /**
     * 修复某种情况下列车永久预定车站而不释放的问题<br>
     * 详见 Create PR<a href="https://github.com/Creators-of-Create/Create/pull/9875">#9875</a>
     */
    @Inject(
        method = "startNavigation",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/content/trains/entity/Navigation;destination:Lcom/simibubi/create/content/trains/station/GlobalStation;",
            opcode = Opcodes.PUTFIELD
        )
    )
    private void cancelInvalidReservation(DiscoveredPath pathTo, CallbackInfoReturnable<Double> cir) {
        if (destination != pathTo.destination && destination != null) {
            destination.cancelReservation(train);
        }
    }

    /**
     * 稍微优化列车寻路逻辑<br>
     * 详见 Create PR<a href="https://github.com/Creators-of-Create/Create/pull/10362">#10362</a>
     */
    @WrapOperation(
        method = "search(DDZLjava/util/ArrayList;Lcom/simibubi/create/content/trains/entity/Navigation$StationTest;)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/trains/entity/Navigation$StationTest;test(DDLjava/util/Map;Lcom/simibubi/create/foundation/utility/Pair;Lcom/simibubi/create/content/trains/station/GlobalStation;)Z",
            ordinal = 1
        )
    )
    private boolean computePenaltyWithDistance(
        Navigation.StationTest stationTest, double distance, double penalty,
        Map<TrackEdge, Pair<Boolean, Couple<TrackNode>>> reachedVia, Pair<Couple<TrackNode>, TrackEdge> current,
        GlobalStation station, Operation<Boolean> original
    ) {
        return original.call(stationTest, distance, distance + penalty, reachedVia, current, station);
    }
}
