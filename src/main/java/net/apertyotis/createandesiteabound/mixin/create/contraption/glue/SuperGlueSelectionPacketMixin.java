package net.apertyotis.createandesiteabound.mixin.create.contraption.glue;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.contraptions.glue.SuperGlueSelectionPacket;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(value = SuperGlueSelectionPacket.class, remap = false)
public abstract class SuperGlueSelectionPacketMixin {

    @Shadow
    private BlockPos from;

    @Shadow
    private BlockPos to;

    // 允许强力胶设置选区时无视方块是否相连，不影响实际连接逻辑
    @WrapOperation(
            method = "lambda$handle$0",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"
            )
    )
    private boolean ignoreGlueGroup(Set<BlockPos> instance, Object o, Operation<Boolean> original) {
        if (!AllConfig.super_glue_always_can_reach) {
            return original.call(instance, o);
        }
        return !from.equals(to);
    }
}
