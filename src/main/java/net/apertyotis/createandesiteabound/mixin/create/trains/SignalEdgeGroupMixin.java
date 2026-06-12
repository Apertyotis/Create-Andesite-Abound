package net.apertyotis.createandesiteabound.mixin.create.trains;

import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.signal.SignalEdgeGroup;
import net.apertyotis.createandesiteabound.foundation.SignalEdgeGroupEx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;
import java.util.function.Consumer;

@Mixin(value = SignalEdgeGroup.class, remap = false)
public abstract class SignalEdgeGroupMixin implements SignalEdgeGroupEx {
    @Shadow
    public Set<SignalEdgeGroup> intersectingResolved;

    @Shadow
    protected abstract void walkIntersecting(Consumer<SignalEdgeGroup> callback);

    // 不考虑预定，仅测试信号区段是否已有其他列车
    @Unique
    @Override
    public boolean caa$isOccupiedUnless(Train train) {
        if (intersectingResolved.isEmpty())
            walkIntersecting(intersectingResolved::add);
        for (SignalEdgeGroup group : intersectingResolved) {
            if ((!group.trains.isEmpty() && (group.trains.size() > 1 || !group.trains.contains(train))))
                return true;
        }
        return false;
    }
}
