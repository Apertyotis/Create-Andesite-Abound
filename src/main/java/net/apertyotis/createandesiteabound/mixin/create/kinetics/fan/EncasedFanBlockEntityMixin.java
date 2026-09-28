package net.apertyotis.createandesiteabound.mixin.create.kinetics.fan;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
public abstract class EncasedFanBlockEntityMixin extends KineticBlockEntity {
    @Shadow
    public abstract void onSpeedChanged(float prevSpeed);

    @Unique
    private KineticScrollValueBehaviour caa$targetSpeed;

    public EncasedFanBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Inject(method = "addBehaviours", at = @At("TAIL"))
    private void speedControlBehaviour(List<BlockEntityBehaviour> behaviours, CallbackInfo ci) {
        caa$targetSpeed = new KineticScrollValueBehaviour(
            Component.translatable("create.kinetics.speed_controller.rotation_speed"), this,
            new CenteredSideValueBoxTransform((state, side) -> side != state.getValue(BlockStateProperties.FACING)));
        caa$targetSpeed.between(-256, 256);
        caa$targetSpeed.withCallback(this::onSpeedChanged);
        behaviours.add(caa$targetSpeed);
    }

    @Override
    public float getSpeed() {
        return caa$targetSpeed == null ? 0 : caa$targetSpeed.getValue();
    }
}
