package net.apertyotis.createandesiteabound.mixin.create.processing.burner;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.NoSuchElementException;

import static com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HEAT_LEVEL;

@Mixin(value = BlazeBurnerBlock.class, remap = false)
public abstract class BlazeBurnerBlockMixin extends HorizontalDirectionalBlock {
    protected BlazeBurnerBlockMixin(Properties pProperties) {
        super(pProperties);
    }

    // 创造烈焰人燃烧室将掉落蛋糕
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Override
    public @NotNull List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        try {
            BlockEntity be = pParams.getParameter(LootContextParams.BLOCK_ENTITY);
            if (be instanceof BlazeBurnerBlockEntity burner && burner.isCreative()) {
                if (pState.getValue(HEAT_LEVEL) == BlazeBurnerBlock.HeatLevel.KINDLED)
                    return List.of(AllBlocks.BLAZE_BURNER.asStack(), AllItems.CREATIVE_BLAZE_CAKE.asStack());
                else if (pState.getValue(HEAT_LEVEL) == BlazeBurnerBlock.HeatLevel.SEETHING)
                    return List.of(AllBlocks.BLAZE_BURNER.asStack(), AllItems.CREATIVE_BLAZE_CAKE.asStack(2));
            }
        } catch (NoSuchElementException | IllegalArgumentException ignored) {
        }

        return super.getDrops(pState, pParams);
    }

    @WrapOperation(
        method = "tryInsert",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlockEntity;applyCreativeFuel()V"
        )
    )
    private static void survivalBlazeCake(
        BlazeBurnerBlockEntity burnerBE, Operation<Void> original,
        @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) Level world,
        @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true, ordinal = 0) boolean doNotConsume,
        @Cancellable CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir
    ) {
        if (!doNotConsume && !world.isClientSide) {
            try {
                if (!burnerBE.isCreative())
                    stack.shrink(1);
                else if (state.getValue(HEAT_LEVEL) == BlazeBurnerBlock.HeatLevel.SEETHING)
                    cir.setReturnValue(InteractionResultHolder.success(AllItems.CREATIVE_BLAZE_CAKE.asStack(2)));
                else
                    stack.shrink(1);
            } catch (IllegalArgumentException ignored) {
            }
        }
        original.call(burnerBE);
    }
}
