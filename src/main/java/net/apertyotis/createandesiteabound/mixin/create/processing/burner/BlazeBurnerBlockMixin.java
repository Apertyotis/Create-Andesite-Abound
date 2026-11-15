package net.apertyotis.createandesiteabound.mixin.create.processing.burner;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.minecraftforge.fluids.FluidUtil.tryEmptyContainerAndStow;

@Mixin(value = BlazeBurnerBlock.class, remap = false)
public abstract class BlazeBurnerBlockMixin{
    @WrapOperation(
        method = "use",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlock;tryInsert(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;ZZZ)Lnet/minecraft/world/InteractionResultHolder;",
            remap = false
        ),
        remap = true
    )
    private InteractionResultHolder<ItemStack> tryFill(
        BlockState state, Level level, BlockPos pos, ItemStack stack,
        boolean doNotConsume, boolean forceOverflow, boolean simulate,
        Operation<InteractionResultHolder<ItemStack>> original,
        @Local(argsOnly = true) Player player, @Local(argsOnly = true) InteractionHand hand
        ) {
        Try: {
            BlockEntity burner = level.getBlockEntity(pos);
            if (burner == null)
                break Try;

            IFluidHandler fluidHandler = burner.getCapability(ForgeCapabilities.FLUID_HANDLER).resolve().orElse(null);
            IItemHandler inventory = player.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
            if (stack.isEmpty() || fluidHandler == null || inventory == null)
                break Try;

            FluidActionResult result = tryEmptyContainerAndStow(stack, fluidHandler, inventory, 1000, player, !simulate);
            if (result.isSuccess()) {
                if (!doNotConsume)
                    player.setItemInHand(hand, result.getResult());
                return InteractionResultHolder.success(ItemStack.EMPTY);
            }
        }

        return original.call(state, level, pos, stack, doNotConsume, forceOverflow, simulate);
    }
}
