package net.apertyotis.createandesiteabound.mixin.create.kinetics.fan;

import com.simibubi.create.content.fluids.tank.BoilerHeaters;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import net.apertyotis.createandesiteabound.AllConfigs;
import net.apertyotis.createandesiteabound.content.fan.FanGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = EncasedFanBlockEntity.class, remap = false)
public abstract class EncasedFanBlockEntityMixin extends KineticBlockEntity implements FanGenerator {
    @Shadow
    public abstract void onSpeedChanged(float prevSpeed);

    @Unique
    private KineticScrollValueBehaviour caa$targetSpeed;

    @Unique
    private boolean caa$reActivateSource;

    @Unique
    private int caa$heatLevel;

    public EncasedFanBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void setLazyTick(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        setLazyTickRate(30);
    }

    @Inject(method = "addBehaviours", at = @At("TAIL"))
    private void speedControlBehaviour(List<BlockEntityBehaviour> behaviours, CallbackInfo ci) {
        caa$targetSpeed = new KineticScrollValueBehaviour(
            Component.translatable("create.kinetics.speed_controller.rotation_speed"), this,
            new CenteredSideValueBoxTransform((state, side) -> side.getAxis() != state.getValue(BlockStateProperties.FACING).getAxis()));
        caa$targetSpeed.between(-256, 256);
        caa$targetSpeed.withCallback(newSpeed -> caa$reActivateSource = true);
        behaviours.add(caa$targetSpeed);
    }

    @Inject(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;tick()V",
            shift = At.Shift.AFTER
        )
    )
    private void generatorTick(CallbackInfo ci) {
        if (caa$reActivateSource) {
            caa$updateGeneratedRotation();
            caa$reActivateSource = false;
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null)
            return;
        int oldHeat = caa$heatLevel;
        if (getBlockState().getValue(BlockStateProperties.FACING) != Direction.DOWN) {
            caa$heatLevel = 0;
        } else {
            BlockPos pos = getBlockPos().below();
            caa$heatLevel = (int) BoilerHeaters.getActiveHeat(level, pos, level.getBlockState(pos)) + 1;
        }

        if (oldHeat != caa$heatLevel) {
            caa$reActivateSource = true;
            if (oldHeat <= 0 && caa$targetSpeed != null && caa$targetSpeed.getValue() == 0) {
                int newSpeed = ((int) speed) == 0 ? 16 : ((int) speed);
                caa$targetSpeed.setValue(newSpeed);
            }
        }
    }

    @Unique
    @Override
    public int caa$getHeatLevel() {
        return caa$heatLevel;
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void writeHeatLevel(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        compound.putInt("HeatLevel", caa$heatLevel);
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void readHeatLevel(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        caa$heatLevel = compound.getInt("HeatLevel");
    }

    @Unique
    public void caa$updateGeneratedRotation() {
        float speed = getGeneratedSpeed();
        float prevSpeed = this.speed;

        if (level == null || level.isClientSide)
            return;

        if (prevSpeed != speed) {
            if (!hasSource()) {
                IRotate.SpeedLevel levelBefore = IRotate.SpeedLevel.of(this.speed);
                IRotate.SpeedLevel levelAfter = IRotate.SpeedLevel.of(speed);
                if (levelBefore != levelAfter)
                    effects.queueRotationIndicators();
            }

            caa$applyNewSpeed(prevSpeed, speed);
        }

        if (hasNetwork() && speed != 0) {
            KineticNetwork network = getOrCreateNetwork();
            caa$notifyStressCapacityChange(calculateAddedStressCapacity());
            getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
            network.updateStress();
        }

        onSpeedChanged(prevSpeed);
        sendData();
    }

    @Unique
    public void caa$applyNewSpeed(float prevSpeed, float speed) {

        // Speed changed to 0
        if (speed == 0) {
            if (hasSource()) {
                caa$notifyStressCapacityChange(0);
                getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
                return;
            }
            detachKinetics();
            setSpeed(0);
            setNetwork(null);
            return;
        }

        // Now turning - create a new Network
        if (prevSpeed == 0) {
            setSpeed(speed);
            setNetwork(caa$createNetworkId());
            attachKinetics();
            return;
        }

        // Change speed when overpowered by other generator
        if (hasSource()) {

            // Staying below Overpowered speed
            if (Math.abs(prevSpeed) >= Math.abs(speed)) {
                if (Math.signum(prevSpeed) != Math.signum(speed)) {
                    // noinspection DataFlowIssue
                    level.destroyBlock(worldPosition, true);
                }
                return;
            }

            // Faster than attached network -> become the new source
            detachKinetics();
            setSpeed(speed);
            source = null;
            setNetwork(caa$createNetworkId());
            attachKinetics();
            return;
        }

        // Reapply source
        detachKinetics();
        setSpeed(speed);
        attachKinetics();
    }

    @Unique
    protected void caa$notifyStressCapacityChange(float capacity) {
        getOrCreateNetwork().updateCapacityFor(this, capacity);
    }

    @Unique
    public Long caa$createNetworkId() {
        return worldPosition.asLong();
    }

    @Override
    public float getSpeed() {
        float value = caa$targetSpeed == null ? 0 : caa$targetSpeed.getValue();
        if (value == 0)
            value = super.getSpeed();
        return value;
    }

    @Override
    public float getGeneratedSpeed() {
        return caa$targetSpeed == null ? 0 : caa$targetSpeed.getValue();
    }

    @Override
    public float calculateStressApplied() {
        return 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float generated = Math.abs(getGeneratedSpeed());
        if (generated == 0 || caa$heatLevel <= 0)
            return 0;
        if (caa$heatLevel == 1)
            return AllConfigs.fan_passive_generation / generated;
        else
            return AllConfigs.fan_active_generation * (caa$heatLevel - 1) / generated;
    }

    @Override
    public void removeSource() {
        if (hasSource() && isSource())
            caa$reActivateSource = true;
        super.removeSource();
    }

    @Override
    public void setSource(BlockPos source) {
        super.setSource(source);
        if (level == null)
            return;
        BlockEntity blockEntity = level.getBlockEntity(source);
        if (!(blockEntity instanceof KineticBlockEntity sourceBE))
            return;
        if (caa$reActivateSource && Math.abs(sourceBE.getSpeed()) >= Math.abs(getGeneratedSpeed()))
            caa$reActivateSource = false;
    }
}
