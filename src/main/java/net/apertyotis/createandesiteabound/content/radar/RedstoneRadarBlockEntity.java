package net.apertyotis.createandesiteabound.content.radar;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedstoneRadarBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private ResourceKey<Level> targetDimension;
    private BlockPos targetPos;
    private State state = State.EMPTY;
    private final Map<String, Integer> remoteSource = new HashMap<>();

    public enum State {
        EMPTY,
        NOT_LOADED,
        NOT_RECEIVER,
        CONNECTED
    }

    public RedstoneRadarBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(200);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        super.read(nbt, clientPacket);

        state = State.values()[nbt.getInt("State")];

        if (nbt.contains("TargetDimension") && nbt.contains("TargetPos")) {
            ResourceLocation id = new ResourceLocation(nbt.getString("TargetDimension"));
            targetDimension = ResourceKey.create(Registries.DIMENSION, id);
            targetPos = NbtUtils.readBlockPos(nbt.getCompound("TargetPos"));
        }

        remoteSource.clear();
        CompoundTag sourceTag = nbt.getCompound("Source");
        for (String key: sourceTag.getAllKeys()) {
            remoteSource.put(key, sourceTag.getInt(key));
        }
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        super.write(nbt, clientPacket);

        nbt.putInt("State", state.ordinal());

        if (targetDimension != null && targetPos != null) {
            nbt.putString("TargetDimension", targetDimension.location().toString());
            nbt.put("TargetPos", NbtUtils.writeBlockPos(targetPos));
        }

        CompoundTag sourceTag = new CompoundTag();
        for (var entry: remoteSource.entrySet()) {
            sourceTag.putInt(entry.getKey(), entry.getValue());
        }
        nbt.put("Source", sourceTag);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (getLevel() == null || getLevel().isClientSide)
            return;

        notifyTarget(false);

        int shouldOutput = remoteSource.values()
                .stream()
                .max(Integer::compareTo)
                .orElse(0);
        shouldOutput = Math.min(shouldOutput, 15);

        if (getBlockState().getValue(RedstoneRadarBlock.OUTPUT) != shouldOutput) {
            getLevel().setBlock(getBlockPos(),
                    getBlockState().setValue(RedstoneRadarBlock.OUTPUT, shouldOutput), 3);
            notifyUpdate();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        notifyTarget(true);
    }

    public void scheduleUpdate() {
        lazyTickCounter = 0;
    }

    public void setTarget(ResourceKey<Level> dim, BlockPos pos) {
        notifyTarget(true);
        targetDimension = dim;
        targetPos = pos;
        notifyTarget(false);
    }

    public String getDimPosString() {
        if (getLevel() == null)
            return "";
        return getLevel().dimensionTypeId().location() + "@" + getBlockPos().toShortString();
    }

    public void notifyTarget(boolean removeSource) {
        if (getLevel() == null || getLevel().isClientSide() || getLevel().getServer() == null)
            return;

        State oldState = state;
        if (targetDimension != null && targetPos != null) {
            ServerLevel otherLevel = getLevel().getServer().getLevel(targetDimension);
            if (otherLevel == null || !otherLevel.isLoaded(targetPos)) {
                state = State.NOT_LOADED;
            } else if (!(otherLevel.getBlockEntity(targetPos) instanceof RedstoneRadarBlockEntity target)) {
                state = State.NOT_RECEIVER;
            } else {
                state = State.CONNECTED;
                String key = getDimPosString();
                if (removeSource) {
                    if (target.remoteSource.remove(key) != null)
                        target.scheduleUpdate();
                } else {
                    int shouldOutput = getBlockState().getValue(RedstoneRadarBlock.INPUT);
                    target.remoteSource.compute(key, (k, v) -> {
                        if (v == null || v != shouldOutput) {
                            target.scheduleUpdate();
                            BlockPos pos = getBlockPos();
                            ((ServerLevel) getLevel()).sendParticles(
                                    new ShriekParticleOption(0),
                                    pos.getX() + 0.5d, pos.getY() + 0.75d, pos.getZ() + 0.5d,
                                    1, 0, 0, 0, 4);
                            getLevel().playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE,
                                    SoundSource.BLOCKS, 0.8f, 0.5f);
                        }
                        return shouldOutput;
                    });
                }
            }
        } else {
            state = State.EMPTY;
        }
        if (oldState != state) {
            sendData();
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("    ")
                .append(Component.translatable("info.caa.goggle.status")));
        switch (state) {
            case EMPTY -> tooltip.add(Component.translatable("info.caa.goggle.empty")
                    .withStyle(ChatFormatting.GRAY));
            case NOT_LOADED -> tooltip.add(Component.translatable("info.caa.goggle.not_loaded")
                        .withStyle(ChatFormatting.RED));
            case NOT_RECEIVER -> tooltip.add(Component.translatable("info.caa.goggle.not_receiver")
                        .withStyle(ChatFormatting.RED));
            case CONNECTED -> tooltip.add(Component.translatable("info.caa.goggle.connected")
                    .withStyle(ChatFormatting.GREEN));
        }
        tooltip.add(Component.literal(""));

        if (state != State.EMPTY && targetDimension != null && targetPos != null) {
            tooltip.add(Component.translatable("info.caa.goggle.target")
                    .withStyle(ChatFormatting.GOLD));
            String dimensionDescId = "dimension." + targetDimension.location().getNamespace() +
                    "." + targetDimension.location().getPath();
            String pos = String.format("%d, %d, %d", targetPos.getX(), targetPos.getY(), targetPos.getZ());

            tooltip.add(Component.literal("    ")
                    .append(Component.translatable(dimensionDescId)
                            .withStyle(ChatFormatting.GRAY)));

            tooltip.add(Component.literal("    ")
                    .append(Component.literal(pos)
                            .withStyle(ChatFormatting.GRAY)));
            tooltip.add(Component.literal(""));
        }

        if (getBlockState().getValue(RedstoneRadarBlock.FORCELOAD)) {
            tooltip.add(Component.translatable("info.caa.goggle.forceload_enable")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.translatable("info.caa.goggle.forceload_disable"));
        }
        return true;
    }
}
