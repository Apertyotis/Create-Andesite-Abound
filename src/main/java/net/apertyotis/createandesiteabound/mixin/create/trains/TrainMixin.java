package net.apertyotis.createandesiteabound.mixin.create.trains;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.CarriageBogey;
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
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;


@Mixin(value = Train.class, remap = false)
public abstract class TrainMixin {
    @Unique
    private boolean caa$isError = false;

    @Unique
    private int caa$msgType;

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

        player.displayClientMessage(Component.translatable("create.train.status", train.name)
                .withStyle(ChatFormatting.GOLD), false);

        String key = switch (caa$msgType) {
            case 0 -> "info.caa.train.occupied";
            case 1 -> "info.caa.train.intrude";
            default -> "";
        };

        CarriageBogey bogey = train.carriages.get(0).leadingBogey();
        Vec3 pos = train.carriages.get(0).leadingBogey().getAnchorPosition();
        ResourceKey<Level> dimension = bogey.getDimension();
        Component position = Component.literal((pos == null ? "???" :
            "(%.1f, %.1f, %.1f)".formatted(pos.x, pos.y, pos.z)) +
            " [" + (dimension == null ? "???" : dimension.location()) + "]");

        player.displayClientMessage(Component.literal(" - ").withStyle(ChatFormatting.GRAY)
                .append(Component.translatable(key).withStyle(st ->
                    st.withColor(0xFFD3B4).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, position)))),
            false);
    }
}
