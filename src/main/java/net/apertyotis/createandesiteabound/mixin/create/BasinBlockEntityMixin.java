package net.apertyotis.createandesiteabound.mixin.create;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BasinBlockEntity.class, remap = false)
public abstract class BasinBlockEntityMixin extends SmartBlockEntity {
    @Shadow
    private boolean contentsChanged;

    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public BasinBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 工作盆也会提醒头顶的工作方块更新（让锅盖类工作方块不必频繁主动搜索配方
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (level == null)
            return;
        if (!contentsChanged)
            return;

        BlockEntity be = level.getBlockEntity(worldPosition.above(1));
        if (be instanceof BasinOperatingBlockEntity boe)
             boe.basinChecker.scheduleUpdate();
    }
}
