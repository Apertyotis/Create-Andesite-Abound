package net.apertyotis.createandesiteabound.mixin.create.equipment;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BacktankBlockEntity.class, remap = false)
public abstract class BacktankBlockEntityMixin {

    @Shadow
    private CompoundTag vanillaTag;

    @Shadow
    private CompoundTag forgeCapsTag;

    /**
     * 修复 Ctrl+中键 复制背罐导致无限递归的问题<br>
     * 详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/7284">#7284</a>
     */
    @WrapMethod(method = "setTags")
    private void setTagsSafe(CompoundTag vanillaTag, CompoundTag forgeCapsTag, Operation<Void> original) {
        this.vanillaTag = vanillaTag.copy();
        this.forgeCapsTag = forgeCapsTag == null ? null : forgeCapsTag.copy();
        // Prevent nesting of the ctrl+pick block added tag
        vanillaTag.remove("BlockEntityTag");
    }
}
