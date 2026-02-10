package net.apertyotis.createandesiteabound.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ControlledContraptionEntity.class, remap = false)
public abstract class ControlledContraptionEntityMixin {
    @Shadow
    protected BlockPos controllerPos;

    /**
     * 修复运动结构会在客户端消失的问题，详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/9824">#8492</a><br>
     * 这个修复大概率是有效的，但是目前无论原修复者还是 Create 开发者都无法确定它为什么有效
     */
    @Inject(method = "readAdditional", at = @At("TAIL"))
    private void redirectReadControllerPos(CompoundTag compound, boolean spawnPacket, CallbackInfo ci) {
        if (compound.contains("ControllerPos")) {
            controllerPos = NbtUtils.readBlockPos(compound.getCompound("ControllerPos"));
        }
    }

    @WrapOperation(
            method = "writeAdditional",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;"
            ),
            remap = true
    )
    private Tag redirectWriteControllerPos(CompoundTag instance, String p_128366_, Tag p_128367_, Operation<Tag> original) {
        return original.call(instance, "ControllerPos", NbtUtils.writeBlockPos(controllerPos));
    }
}
