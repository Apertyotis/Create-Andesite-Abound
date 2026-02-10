package net.apertyotis.createandesiteabound.mixin.create;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PortableItemInterfaceBlockEntity.class, remap = false)
public class PortableItemInterfaceBlockEntityMixin {
    /**
     * 修复漏斗无法及时发现移动物品接口中物品的问题<br>
     * 详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/9624">#9624</a>
     */
    @Inject(method = "startTransferringTo", at = @At("TAIL"))
    private void afterStartTransferringTo(Contraption contraption, float distance, CallbackInfo ci) {
        BlockEntity be = (BlockEntity)(Object) this;
        Level level = be.getLevel();
        if (level != null && !level.isClientSide)
            level.updateNeighborsAt(be.getBlockPos(), be.getBlockState().getBlock());
    }

    @Inject(method = "stopTransferring", at = @At("TAIL"))
    private void afterStopTransferring(CallbackInfo ci) {
        BlockEntity be = (BlockEntity)(Object) this;
        Level level = be.getLevel();
        if (level != null && !level.isClientSide)
            level.updateNeighborsAt(be.getBlockPos(), be.getBlockState().getBlock());
    }
}
