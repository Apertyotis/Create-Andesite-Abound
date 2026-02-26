package net.apertyotis.createandesiteabound.mixin.create.equipment;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.equipment.armor.BacktankBlock;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Optional;

@Mixin(value = BacktankBlock.class, remap = false)
public abstract class BacktankBlockMixin {
    /**
     * 修复 Ctrl+中键 复制背罐导致无限递归的问题<br>
     * 详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/7284">#7284</a>
     */
    @ModifyExpressionValue(
            method = "getCloneItemStack",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;"
            ),
            slice = @Slice(to = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;",
                    ordinal = 1
            )),
            remap = true
    )
    private Optional<CompoundTag> copyBeforeUse(Optional<CompoundTag> original) {
        return original.map(CompoundTag::copy);
    }
}