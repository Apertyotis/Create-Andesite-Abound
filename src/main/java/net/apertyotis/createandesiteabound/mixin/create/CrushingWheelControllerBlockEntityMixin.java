package net.apertyotis.createandesiteabound.mixin.create;

import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingInventory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.apertyotis.createandesiteabound.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = CrushingWheelControllerBlockEntity.class, remap = false)
public abstract class CrushingWheelControllerBlockEntityMixin extends SmartBlockEntity {

    @Shadow
    public float crushingspeed;

    @Shadow
    public ProcessingInventory inventory;

    @Shadow
    public abstract Optional<ProcessingRecipe<RecipeWrapper>> findRecipe();

    @Shadow
    protected abstract void applyRecipe();

    // 创建空构造函数来通过编译器语法检查，没有实际作用
    public CrushingWheelControllerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 修改配方时间，抵消西米布比神秘的条件判断影响，并设置默认配方时间为30
    @Inject(method = "itemInserted", at = @At("HEAD"), cancellable = true)
    private void modifyProcessingDuration(ItemStack stack, CallbackInfo ci) {
        if (!Config.crushing_wheel_speed_change) return;

        Optional<ProcessingRecipe<RecipeWrapper>> recipe = findRecipe();
        inventory.remainingTime = recipe.isPresent() ? recipe.get().getProcessingDuration() + 20 : 50;
        inventory.appliedRecipe = false;
        ci.cancel();
    }

    // 修改工作速度，使得256rpm处理整组物品时为1倍速1倍效率，非整组时统一为0.5倍效率
    @ModifyVariable(
            method = "tick",
            at = @At(value = "STORE"),
            name = "processingSpeed"
    )
    private float modifyProcessingSpeed(float original) {
        if (!Config.crushing_wheel_speed_change) return original;

        if (inventory.appliedRecipe) return 1;

        float speed = crushingspeed * 50 / 256;
        int count = inventory.getStackInSlot(0).getCount();
        if (count > 0 && count < 64) {
            speed = speed * 32f / count;
        }

        return speed;
    }

    // 取消输出的1tick延迟
    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/crusher/CrushingWheelControllerBlockEntity;spawnParticles(Lnet/minecraft/world/item/ItemStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void processImmediately(CallbackInfo ci) {
        if (!Config.crushing_wheel_speed_change) return;

        if (level == null || level.isClientSide)
            return;

        if (inventory.remainingTime <= 20 && !inventory.appliedRecipe) {
            applyRecipe();
            inventory.appliedRecipe = true;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);
        }
    }
}
