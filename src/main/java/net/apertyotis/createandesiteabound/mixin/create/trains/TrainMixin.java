package net.apertyotis.createandesiteabound.mixin.create.trains;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.signal.SignalEdgeGroup;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;
import net.apertyotis.createandesiteabound.foundation.SignalEdgeGroupEx;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;


@Mixin(value = Train.class, remap = false)
public abstract class TrainMixin {
    @Unique
    private boolean caa$isError = false;

    @Unique
    private int caa$msgType;

    @Unique
    private List<String> caa$relevantTrains = new ArrayList<>();

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
            UUID groupId = signal.getGroup(couple.getSecond().getSecond());
            Iterator<UUID> iter = train.reservedSignalBlocks.iterator();
            while (true) {
                SignalEdgeGroup signalEdgeGroup = Create.RAILWAYS.signalEdgeGroups.get(groupId);
                if (signalEdgeGroup != null) {
                    for (Train other: signalEdgeGroup.trains) {
                        caa$relevantTrains.add(other.name.getString());
                    }
                }
                if (iter.hasNext())
                    groupId = iter.next();
                else
                    break;
            }

            train.reservedSignalBlocks.clear();
            caa$isError = true;
            caa$msgType = 0;
        } else {
            UUID groupId = signal.getGroup(couple.getSecond().getSecond());
            SignalEdgeGroup signalEdgeGroup = Create.RAILWAYS.signalEdgeGroups.get(groupId);
            if (signalEdgeGroup == null)
                return;

            // 列车进入的区段存在其他列车
            if (((SignalEdgeGroupEx) signalEdgeGroup).caa$isOccupiedUnless(train)) {
                caa$isError = true;
                caa$msgType = 1;
                caa$relevantTrains.add(train.name.getString());
                for (Train other: signalEdgeGroup.trains) {
                    caa$relevantTrains.add(other.name.getString());
                }
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(Level level, CallbackInfo ci) {
        if (caa$isError)
            caa$isError = false;
        else
            return;

        Train train = (Train)(Object) this;
        LivingEntity owner = train.getOwner(level);
        if (!(owner instanceof Player player))
            return;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < caa$relevantTrains.size(); i++) {
            builder.append(caa$relevantTrains.get(i));
            if (i + 1 < caa$relevantTrains.size())
                builder.append(", ");
        }
        caa$relevantTrains.clear();

        switch (caa$msgType) {
            case 0:
                player.displayClientMessage(Component.translatable("info.caa.train.occupied", train.name.getString())
                        .withStyle(ChatFormatting.GOLD), false);
                player.displayClientMessage(
                        Component.translatable("info.caa.train.relevant", builder.toString()), false);
                break;
            case 1:
                player.displayClientMessage(Component.translatable("info.caa.train.intrude", train.name.getString())
                        .withStyle(ChatFormatting.GOLD), false);
                player.displayClientMessage(
                        Component.translatable("info.caa.train.relevant", builder.toString()), false);
                break;
            default:
                break;
        }
    }
}
