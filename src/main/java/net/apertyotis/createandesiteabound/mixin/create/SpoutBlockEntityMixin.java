package net.apertyotis.createandesiteabound.mixin.create;

import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SpoutBlockEntity.class, remap = false)
public abstract class SpoutBlockEntityMixin extends SmartBlockEntity {

    @Shadow
    public int processingTicks;

    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public SpoutBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 减少注液器1tick工作时间
    @Inject(method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/fluids/spout/SpoutBlockEntity;processingTicks:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void afterTick(CallbackInfo ci) {
        if (processingTicks == 19) processingTicks--;
    }

    // 设定储罐大小为2000mb
    @ModifyArg(
            method = "addBehaviours",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/fluid/SmartFluidTankBehaviour;single(Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;I)Lcom/simibubi/create/foundation/blockEntity/behaviour/fluid/SmartFluidTankBehaviour;"
            )
    )
    private int modifyTankCapacity(int original) {
        return 2000;
    }
}
