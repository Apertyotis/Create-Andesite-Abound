package net.apertyotis.createandesiteabound.mixin.create;

import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.apertyotis.createandesiteabound.Config;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = PressingBehaviour.class, remap = false)
public abstract class PressBehaviourMixin extends BeltProcessingBehaviour {
    @Shadow
    public List<ItemStack> particleItems;

    @Shadow
    public PressingBehaviour.PressingBehaviourSpecifics specifics;

    @Shadow
    public int runningTicks;

    @Shadow
    public boolean running;

    @Shadow
    public boolean finished;

    @Shadow
    public PressingBehaviour.Mode mode;

    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public PressBehaviourMixin(SmartBlockEntity be) {
        super(be);
    }

    // 在辊压机工作结束后，提前1gt检查是否完成
    @Inject(method = "tick", at = @At("TAIL"))
    private void afterTick(CallbackInfo ci){
        if (!Config.press_speed_change) return;
        // 工作盆就算了
        if (mode == PressingBehaviour.Mode.BASIN) return;
        Level level = getWorld();
        if (level != null && !level.isClientSide && runningTicks > 240) {
            finished = true;
            running = false;
            particleItems.clear();
            specifics.onPressingCompleted();
            blockEntity.sendData();
        }
    }
}
