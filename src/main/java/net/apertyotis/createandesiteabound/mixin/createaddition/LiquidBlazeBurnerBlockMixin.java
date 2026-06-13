package net.apertyotis.createandesiteabound.mixin.createaddition;

import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlock;
import com.simibubi.create.AllItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LiquidBlazeBurnerBlock.class, remap = false)
public abstract class LiquidBlazeBurnerBlockMixin {
    // 防止非创造玩家对流体烈焰人燃烧室使用创造蛋糕
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void unusableSurvivalBlazeCake(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!player.isCreative() && AllItems.CREATIVE_BLAZE_CAKE.isIn(player.getItemInHand(hand))) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
