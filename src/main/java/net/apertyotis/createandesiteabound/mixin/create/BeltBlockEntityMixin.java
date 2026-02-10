package net.apertyotis.createandesiteabound.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BeltBlockEntity.class, remap = false)
public class BeltBlockEntityMixin extends KineticBlockEntity {
    @Unique
    public boolean caa$markDirty;

    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public BeltBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void sendData() {
        super.sendData();
        caa$markDirty = true;
    }

    /**
     * 部分修复传送带刷物品问题，详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/8335">#8335</a>
     */
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/belt/transport/BeltInventory;tick()V"
            )
    )
    private void beltInventoryTickWrapper(BeltInventory instance, Operation<Void> original) {
        caa$markDirty =false;
        original.call(instance);
        if (caa$markDirty) {
            setChanged();
        }
    }
}
