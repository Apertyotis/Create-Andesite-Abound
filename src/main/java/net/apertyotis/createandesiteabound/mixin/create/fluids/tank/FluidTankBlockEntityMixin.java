package net.apertyotis.createandesiteabound.mixin.create.fluids.tank;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FluidTankBlockEntity.class, remap = false)
public abstract class FluidTankBlockEntityMixin extends SmartBlockEntity {

    @Shadow
    protected boolean window;

    @Shadow
    protected LazyOptional<IFluidHandler> fluidCapability;

    // 空构造函数，无实际作用
    public FluidTankBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * 修复蓝图打印储罐会错误地设置宽高为 0 的问题<br>
     * 详见 Create Issue <a href="https://github.com/Creators-of-Create/Create/issues/7137">#7137</a><br>
     * 另见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/10525">#10525</a>
     */
    @Override
    public void writeSafe(CompoundTag tag) {
        if (((FluidTankBlockEntity)(Object) this).isController()) {
            tag.putBoolean("Window", window);
            tag.putInt("Size", 1);
            tag.putInt("Height", 1);
        }
    }

    @Inject(method = "invalidate", at = @At("TAIL"))
    private void invalidateTank(CallbackInfo ci) {
        fluidCapability.invalidate();
    }
}
