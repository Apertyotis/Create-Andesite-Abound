package net.apertyotis.createandesiteabound.content.belt;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.BulkScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class BeltScrollValueBehaviour extends BulkScrollValueBehaviour {

    public BeltScrollValueBehaviour(Component label, SmartBlockEntity sbe, ValueBoxTransform slot) {
        super(label, sbe, slot, be -> {
            Level level = be.getLevel();
            if (level != null && be instanceof BeltBlockEntity belt) {
                return BeltBlock.getBeltChain(level, belt.getController())
                    .stream()
                    .map(pos -> (BeltBlockEntity) level.getBlockEntity(pos))
                    .toList();
            }
            return List.of();
        });
        withFormatter(v -> v == 0 ? "*" : String.valueOf(Math.abs(v)));
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        ImmutableList<Component> rows = ImmutableList.of(
            Components.literal("⟳").withStyle(ChatFormatting.BOLD),
            Components.literal("⟲").withStyle(ChatFormatting.BOLD));
        ValueSettingsFormatter formatter = new ValueSettingsFormatter(this::formatSettings);
        return new ValueSettingsBoard(label, 256, 32, rows, formatter);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void newSettingHovered(ValueSettings vs) {
        for (SmartBlockEntity be : getBulk())
            if (be instanceof BeltBlockEntityEx belt)
                belt.caa$setTargetSpeed(vs.row() == 0 ? -vs.value() : vs.value());
    }

    public MutableComponent formatSettings(ValueSettings vs) {
        if (vs.value() == 0){
            return Lang.text("*").component();
        } else {
            return Lang.number(Math.abs(vs.value()))
                .add(Lang.text(vs.row() == 0 ? "⟳" : "⟲").style(ChatFormatting.BOLD))
                .component();
        }
    }

    /**
     * 改设定无视玩家是否按下 Ctrl，始终对整条传送带生效<br>
     * @See
     */
    @Override
    public void setValueSettings(Player player, ValueSettings vs, boolean ctrlHeld) {
        if (!vs.equals(getValueSettings()))
            playFeedbackSound(this);
        for (SmartBlockEntity be: getBulk()) {
            ScrollValueBehaviour other = be.getBehaviour(ScrollValueBehaviour.TYPE);
            if (other != null)
                other.setValue(vs.row() == 0 ? -vs.value() : vs.value());
        }
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(value <= 0 ? 0 : 1, Math.abs(value));
    }
}
