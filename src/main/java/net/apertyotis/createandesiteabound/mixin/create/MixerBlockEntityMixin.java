package net.apertyotis.createandesiteabound.mixin.create;

import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = MechanicalMixerBlockEntity.class, remap = false)
public abstract class MixerBlockEntityMixin extends BasinOperatingBlockEntity {

    @Shadow
    public int runningTicks;

    @Shadow
    public int processingTicks;

    @Shadow
    public boolean running;

    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public MixerBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    // 提前计算配方时间，一方面替代了目标原先计算方法，一方面减少了1gt机器延迟
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (level == null || level.isClientSide) return;
        // 计算配方时间
        if (running && runningTicks == 20 && processingTicks < 0) {
            float recipeSpeed = 1;
            if (currentRecipe instanceof ProcessingRecipe) {
                int t = ((ProcessingRecipe<?>) currentRecipe).getProcessingDuration();
                if (t != 0)
                    recipeSpeed = t / 100f;
            }

            // 调整了不恰当的log2函数使用，并再减少1gt时间
            processingTicks = Mth.clamp((Mth.ceillog2((int) (512 / speed))) * Mth.ceil(recipeSpeed * 15), 1, 512);

            // 后续为目标原先服务端播放音效逻辑
            Optional<BasinBlockEntity> basin = getBasin();
            if (basin.isPresent()) {
                Couple<SmartFluidTankBehaviour> tanks = basin.get().getTanks();
                if (!tanks.getFirst().isEmpty() || !tanks.getSecond().isEmpty())
                    level.playSound(null, worldPosition, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
                            SoundSource.BLOCKS, .75f, speed < 65 ? .75f : 1.5f);
            }
        }
    }

    // 增加1tick启动延迟，来抵消抬头过快的bug
    @Inject(method = "startProcessingBasin", at = @At("TAIL"))
    private void afterStartProcessingBasin(CallbackInfo ci) {
        runningTicks = -1;
    }
}
